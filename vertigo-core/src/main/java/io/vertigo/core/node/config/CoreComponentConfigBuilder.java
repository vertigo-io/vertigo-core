/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * This class allows to configure a component step by step.
 *
 * @author npiedeloup, pchretien
 */
final class CoreComponentConfigBuilder implements Builder<CoreComponentConfig> {
	private String myId;
	private final boolean proxy;
	private Class<? extends CoreComponent> myApiClass;
	private Class<? extends CoreComponent> myImplClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor of a component config
	 * @param proxy if the component is a proxy
	 */
	CoreComponentConfigBuilder(final boolean proxy) {
		this.proxy = proxy;
	}

	CoreComponentConfigBuilder withPlugin(final Class<? extends Plugin> implClass, List<Param> params, String id) {
		Assertion.checkNotNull(implClass);
		//-----
		myImplClass = implClass;
		return addParams(params)
				.withId(id);
	}

	CoreComponentConfigBuilder withConnector(final Class<? extends Connector> implClass, List<Param> params, String id) {
		Assertion.checkNotNull(implClass);
		//-----
		myImplClass = implClass;
		return addParams(params)
				.withId(id);
	}

	//	CoreComponentConfigBuilder withComponent (final Class<? extends Connector> implClass, List<Param> params, String id) {
	//		
	//	}

	//	CoreComponentConfigBuilder withProxyComponent (final Class<? extends Connector> implClass, List<Param> params, String id) {
	//		
	//	}

	/**
	 * @param implClass the impl class of the component
	 * @return this builder
	 */
	CoreComponentConfigBuilder withImpl(final Class<? extends Component> implClass) {
		Assertion.checkNotNull(implClass);
		//-----
		myImplClass = implClass;
		return this;
	}

	/**
	 * @param apiClass the apiClass for the component
	 * @return this builder
	 */
	CoreComponentConfigBuilder withApi(final Class<? extends Component> apiClass) {
		Assertion.checkNotNull(apiClass);
		//-----
		myApiClass = apiClass;
		return this;
	}

	/**
	 * Adds a list of params to this component config.
	 * @param params the list of params
	 * @return this builder
	 */
	CoreComponentConfigBuilder addParams(final List<Param> params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(params);
		return this;
	}

	/**
	 * Specifies the id to be used (otherwise an id will be chosen by convention) see build method.
	 * @param id the id to use
	 * @return this builder
	 */
	CoreComponentConfigBuilder withId(final String id) {
		Assertion.checkArgNotEmpty(id);
		//---
		myId = id;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public CoreComponentConfig build() {
		Assertion.checkNotNull(myId);
		//---
		final Optional<Class<? extends CoreComponent>> apiClassOpt = Optional.ofNullable(myApiClass);
		final Optional<Class<? extends CoreComponent>> implClassOpt = Optional.ofNullable(myImplClass);
		return new CoreComponentConfig(myId, proxy, apiClassOpt, implClassOpt, myParams);
	}
}
