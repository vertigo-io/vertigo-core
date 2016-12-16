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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - a implemenation class.
 *  - an optional api class.
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 */
public final class ComponentConfig {
	private final String id;
	private final Class<? extends Component> implClass;
	private final Optional<Class<? extends Component>> apiClass;
	private final Map<String, String> params;

	/**
	 * Constructor.
	 * @param optionalApiClass api of the component
	 * @param implClass impl class of the component
	 * @param params params
	 */
	ComponentConfig(
			final String id,
			final Optional<Class<? extends Component>> optionalApiClass,
			final Class<? extends Component> implClass,
			final List<Param> params) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(optionalApiClass);
		Assertion.checkNotNull(implClass);
		Assertion.when(optionalApiClass.isPresent()).check(() -> Component.class.isAssignableFrom(optionalApiClass.get()), "api class {0} must extend {1}", optionalApiClass, Component.class);
		Assertion.checkArgument(Component.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Component.class);
		Assertion.checkNotNull(params);
		//-----
		this.id = id;
		//-----
		this.apiClass = optionalApiClass;
		this.implClass = implClass;

		this.params = params
				.stream()
				.collect(Collectors.toMap(Param::getName, Param::getValue));
	}

	/**
	 * @return impl class of the component
	 */
	public Class<? extends Component> getImplClass() {
		return implClass;
	}

	/**
	 * @return api of the component
	 */
	public Optional<Class<? extends Component>> getApiClass() {
		return apiClass;
	}

	/**
	 * @return id of the component
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return id;
	}
}
