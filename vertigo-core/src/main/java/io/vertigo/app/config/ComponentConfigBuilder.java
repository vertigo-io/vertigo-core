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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.core.param.Param;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;

/**
 * This class allows to configure a component step by step.
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfigBuilder implements Builder<ComponentConfig> {
	private String myId;
	private final Optional<Class<? extends Component>> optionalApiClass;
	private final Class<? extends Component> implClass;
	private final List<Param> myParams = new ArrayList<>();

	/**
	 * Constructor of a component config
	 * @param apiClassOpt and optional apiClass for the component
	 * @param implClass the impl class of the component
	 */
	public ComponentConfigBuilder(
			final Optional<Class<? extends Component>> apiClassOpt,
			final Class<? extends Component> implClass) {
		Assertion.checkNotNull(apiClassOpt);
		Assertion.checkNotNull(implClass);
		//-----
		this.optionalApiClass = apiClassOpt;
		this.implClass = implClass;
	}

	/**
	 * Adds a list of params to this component config.
	 * @param params the list of params
	 * @return this builder
	 */
	public ComponentConfigBuilder addParams(final List<Param> params) {
		Assertion.checkNotNull(params);
		//-----
		myParams.addAll(params);
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
		return addParams(Arrays.asList(params));
	}

	/**
	 * Adds a param to this component config.
	 * @param param the param
	 * @return this builder
	 */
	public ComponentConfigBuilder addParam(final Param param) {
		Assertion.checkNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	public ComponentConfigBuilder withId(final String id) {
		Assertion.checkArgNotEmpty(id);
		//---
		this.myId = id;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		if (myId == null) {
			//Par convention l'id du composant manager est le simpleName de la classe de l'api ou de l'impl.
			myId = DIAnnotationUtil.buildId(optionalApiClass.orElse(implClass));
		}
		return new ComponentConfig(myId, optionalApiClass, implClass, myParams);
	}

}
