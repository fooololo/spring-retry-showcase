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

/**
 * {@link Classifier}的基本实现类. 提供了一些默认的行为和一些方便的成员,比如 constants
 *
 * @author Dave Syer
 * 
 */
public class ClassifierSupport<C, T> implements Classifier<C, T> {

	final private T defaultValue;

	/**
	 * @param defaultValue
	 */
	public ClassifierSupport(T defaultValue) {
		super();
		this.defaultValue = defaultValue;
	}

	/**
	 * 总是返回默认值,可以处理null.
	 *
	 * @see org.springframework.classify.Classifier#classify(Object)
	 */
	public T classify(C throwable) {
		return defaultValue;
	}

}
