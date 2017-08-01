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
package io.vertigo.app.config;

import io.vertigo.core.component.proxy.ProxyFactory;
import io.vertigo.lang.Assertion;

/**
 * The ProxyConfig class defines the way to create a new proxy on an interface.
 *
 * this class is composed of
 *  - an interface identified by an annotation
 *  - an factory that creates the proxy
 *
 * @author pchretien
 */
public final class ProxyFactoryConfig {
	private final Class<? extends ProxyFactory> proxyFactoryClass;

	/**
	 * Constructor.
	 */
	ProxyFactoryConfig(final Class<? extends ProxyFactory> proxyFactoryClass) {
		Assertion.checkNotNull(proxyFactoryClass);
		//-----
		this.proxyFactoryClass = proxyFactoryClass;
	}

	/**
	 * this class is used to create the factory of proxies.
	 * this class is created by Dependency Injection using the components already register.
	 *
	 * @return the proxy factory class
	 */
	public Class<? extends ProxyFactory> getProxyFactoryClass() {
		return proxyFactoryClass;
	}
}
