/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.component.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import io.vertigo.lang.Assertion;

/**
 * Marks a method to be proxied a simple annotation
 *
 * @author pchretien
 * @param <A> the type of annotation used to mark the methods
 */
public interface Proxy<A extends Annotation> {

	/**
	 * Find the annotation (one and only one)
	 * @param method the method
	 * @return the annotation
	 */
	default A findProxyAnnotation(final Method method) {
		Assertion.checkNotNull(method);
		//---
		//We seek all annotations annotated by @ProxyAnnotation
		//There must be one and only one
		final Annotation annotation = Arrays.stream(method.getAnnotations())
				.filter(a -> a.annotationType().isAnnotationPresent(ProxyAnnotation.class))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No way to find a proxy annotaion on method : " + method));

		if (annotation.annotationType().equals(getAnnotationType())) {
			return (A) annotation;
		}
		throw new IllegalStateException("unknown type of annotation on method" + method);
	}

	/**
	 * Executes the methods with args as a function
	 * @param method the method
	 * @param args the args
	 * @return the result
	 */
	Object invoke(final Method method, final Object[] args);

	/**
	* @return the annotation that must be used to mark the methods
	*/
	Class<A> getAnnotationType();

}
