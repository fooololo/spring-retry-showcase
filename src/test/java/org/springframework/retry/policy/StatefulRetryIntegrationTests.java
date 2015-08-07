/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.retry.policy;

import org.junit.Test;
import org.springframework.retry.*;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Dave Syer
 * @author Gary Russell
 * 
 */
public class StatefulRetryIntegrationTests {

	@Test
	public void testExternalRetryWithFailAndNoRetry() throws Throwable {
		MockRetryCallback callback = new MockRetryCallback();

		RetryState retryState = new DefaultRetryState("foo");

		RetryTemplate retryTemplate = new RetryTemplate();
		MapRetryContextCache cache = new MapRetryContextCache();
		retryTemplate.setRetryContextCache(cache);
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(1, Collections
				.<Class<? extends Throwable>, Boolean> singletonMap(Exception.class, true)));

		assertFalse(cache.containsKey("foo"));

		try {
			retryTemplate.execute(callback, retryState);
			// The first failed attempt we expect to retry...
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertEquals(null, e.getMessage());
		}

		assertTrue(cache.containsKey("foo"));

		try {
			retryTemplate.execute(callback, retryState);
			// We don't get a second attempt...
			fail("Expected ExhaustedRetryException");
		}
		catch (ExhaustedRetryException e) {
			// This is now the "exhausted" message:
			assertNotNull(e.getMessage());
		}

		assertFalse(cache.containsKey("foo"));

		// Callback is called once: the recovery path should be called in
		// handleRetryExhausted (so not in this test)...
		assertEquals(1, callback.attempts);
	}

	@Test
	public void testExternalRetryWithSuccessOnRetry() throws Throwable {
		MockRetryCallback callback = new MockRetryCallback();

		RetryState retryState = new DefaultRetryState("foo");

		RetryTemplate retryTemplate = new RetryTemplate();
		MapRetryContextCache cache = new MapRetryContextCache();
		retryTemplate.setRetryContextCache(cache);
		retryTemplate.setRetryPolicy(new SimpleRetryPolicy(2, Collections
				.<Class<? extends Throwable>, Boolean> singletonMap(Exception.class, true)));

		assertFalse(cache.containsKey("foo"));

		Object result = "start_foo";
		try {
			result = retryTemplate.execute(callback, retryState);
			// The first failed attempt we expect to retry...
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertNull(e.getMessage());
		}
		assertTrue(cache.containsKey("foo"));
		assertEquals(1, callback.attempts);

		result = retryTemplate.execute(callback, retryState);

		assertFalse(cache.containsKey("foo"));

		assertEquals(2, callback.attempts);
		assertEquals("bar", result);
	}

	@Test
	public void testExponentialBackOffIsExponential() throws Throwable {
		ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
		policy.setInitialInterval(100);
		policy.setMultiplier(1.5);
		RetryTemplate template = new RetryTemplate();
		template.setBackOffPolicy(policy);
		final List<Long> times = new ArrayList<Long>();
		RetryState retryState = new DefaultRetryState("bar");
		for (int i = 0; i < 3; i++) {
			try {
				template.execute(new RetryCallback<String, Exception>() {
					public String doWithRetry(RetryContext context) throws Exception {
						times.add(System.currentTimeMillis());
						throw new Exception("Fail");
					}
				}, new RecoveryCallback<String>() {
					public String recover(RetryContext context)
							throws Exception {
						return null;
					}
				}, retryState);
			}
			catch (Exception e) {
				assertTrue(e.getMessage().equals("Fail"));
			}
		}
		assertEquals(3, times.size());
		assertTrue(times.get(1) - times.get(0) >= 100);
		assertTrue(times.get(2) - times.get(1) >= 150);
	}

	/**
	 * @author Dave Syer
	 * 
	 */
	private static final class MockRetryCallback implements RetryCallback<String, Exception> {
		int attempts = 0;

		public String doWithRetry(RetryContext context) throws Exception {
			attempts++;
			if (attempts < 2) {
				throw new RuntimeException();
			}
			return "bar";
		}
	}

}
