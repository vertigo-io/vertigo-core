/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import java.util.List;
import java.util.Optional;

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Component;

/**
 * This class allows to configure a component step by step.
 *
 * @author npiedeloup, pchretien
 * @param <B> the type of the parent builder
 */
public final class ComponentConfigBuilder<B extends Builder> implements Builder<ComponentConfig> {
	//Par convention l'id du composant manager est le simpleName de la classe de l'api ou de l'impl.
	private final Optional<Class<? extends Component>> optionalApiClass;
	private final Class<? extends Component> implClass;
	private final List<Param> myParams = new ArrayList<>();

	public ComponentConfigBuilder(final Optional<Class<? extends Component>> optionalApiClass, final Class<? extends Component> implClass) {
		Assertion.checkNotNull(optionalApiClass);
		Assertion.checkNotNull(implClass);
		//-----
		this.optionalApiClass = optionalApiClass;
		this.implClass = implClass;
	}

	/**
	 * Adds a param to this component config.
	 * @param param the param
	 * @return this builder
	 */
	public ComponentConfigBuilder<B> addParam(final Param param) {
		Assertion.checkNotNull(param);
		//-----
		myParams.add(param);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		final String id = optionalApiClass.isPresent() ? DIAnnotationUtil.buildId(optionalApiClass.get()) : DIAnnotationUtil.buildId(implClass);
		return new ComponentConfig(id, optionalApiClass, implClass, myParams);
	}
}
