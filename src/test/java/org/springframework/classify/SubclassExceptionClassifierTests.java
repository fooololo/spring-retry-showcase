/*
 * Copyright 2006-2007 the original author or authors.
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

package org.springframework.classify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.springframework.classify.SubclassClassifier;

public class SubclassExceptionClassifierTests {

	SubclassClassifier<Throwable, String> classifier = new SubclassClassifier<Throwable, String>();

	@Test
	public void testClassifyNullIsDefault() {
		assertEquals(classifier.classify(null), classifier.getDefault());
	}

	@Test
	public void testClassifyNull() {
		assertNull(classifier.classify(null));
	}

	@Test
	public void testClassifyNullNonDefault() {
		classifier = new SubclassClassifier<Throwable, String>("foo");
		assertEquals("foo", classifier.classify(null));
	}

	@Test
	public void testClassifyRandomException() {
		assertNull(classifier.classify(new IllegalStateException("Foo")));
	}

	@Test
	public void testClassifyExactMatch() {
		classifier.setTypeMap(Collections.<Class<? extends Throwable>, String> singletonMap(
				IllegalStateException.class, "foo"));
		assertEquals("foo", classifier.classify(new IllegalStateException("Foo")));
	}

	@Test
	public void testClassifySubclassMatch() {
		classifier.setTypeMap(Collections.<Class<? extends Throwable>, String> singletonMap(RuntimeException.class,
				"foo"));
		assertEquals("foo", classifier.classify(new IllegalStateException("Foo")));
	}

	@Test
	public void testClassifySuperclassDoesNotMatch() {
		classifier.setTypeMap(Collections.<Class<? extends Throwable>, String> singletonMap(
				IllegalStateException.class, "foo"));
		assertEquals(classifier.getDefault(), classifier.classify(new RuntimeException("Foo")));
	}

	@SuppressWarnings("serial")
	@Test
	public void testClassifyAncestorMatch() {
		classifier.setTypeMap(new HashMap<Class<? extends Throwable>, String>() {
			{
				put(Exception.class, "foo");
				put(IllegalArgumentException.class, "bar");
				put(RuntimeException.class, "spam");
			}
		});
		assertEquals("spam", classifier.classify(new IllegalStateException("Foo")));
	}
}
