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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Builder;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.param.Param;

/**
 * This class allows to configure a component step by step.
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfigBuilder implements Builder<ComponentConfig> {
	private Class<? extends Component> myApiClass;
	private Class<? extends Component> myImplClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor of a component config
	 */
	ComponentConfigBuilder() {
	}

	/**
	 * @param implClass the impl class of the component
	 * @return this builder
	 */
	public ComponentConfigBuilder withImpl(final Class<? extends Component> implClass) {
		Assertion.checkNotNull(implClass);
		//-----
		myImplClass = implClass;
		return this;
	}

	/**
	 * @param apiClass the apiClass for the component
	 * @return this builder
	 */
	public ComponentConfigBuilder withApi(final Class<? extends Component> apiClass) {
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
	public ComponentConfigBuilder addParams(final Param[] params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(Arrays.asList(params));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		final Optional<Class<? extends Component>> apiClassOpt = Optional.ofNullable(myApiClass);
		return new ComponentConfig(apiClassOpt, myImplClass, myParams);
	}
}
