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

import io.vertigo.lang.Assertion;

/**
 * Proxy Factory.
 *
 * use cases
 *  - dynamically implementations (sql client, http client)
 *
 * @author pchretien
 */
public interface ProxyFactory {
	/**
	 * creates a proxy from an interface.
	 * @param intf the interface to proxified
	 * @return the proxy
	 */
	default <C> C createProxy(final Class<C> intf) {
		Assertion.checkNotNull(intf);
		Assertion.checkArgument(intf.isInterface(), "only interface can be proxified");
		Assertion.checkArgument(intf.isAnnotationPresent(ProxyAnnotation.class), "only interface annotated by the annotation {0} can be proxified", ProxyAnnotation.class.getSimpleName());
		//---
		return doCreateProxy(intf);
	}

	/**
	 * creates a proxy from an interface.
	 * @param intf the interface to proxified
	 * @return the proxy
	 */
	<C> C doCreateProxy(Class<C> intf);

	/**
	* @return the annotation that tagged any interface concerned by this factory
	*/
	Class<? extends ProxyAnnotation> getAnnotationType();
}
