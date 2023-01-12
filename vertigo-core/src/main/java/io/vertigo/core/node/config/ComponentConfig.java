/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.param.Param;

/**
 * The componentconfig class defines the configuration of a component.
 *
 * A component is defined by
 *  - an id.
 *  - proxy or pure component -no proxy-
 *  - a implemenation class (empty if proxy, required if not proxy)
 *  - an optional api class. (required if proxy)
 *  - a map of params
 *
 * @author npiedeloup, pchretien
 * 
 * @param apiClassOpt api of the component
 * @param implClass impl class of the component
 * @param params params
 */
public record ComponentConfig(
		Optional<Class<? extends Component>> apiClassOpt,
		Class<? extends Component> implClass,
		List<Param> params) {

	public ComponentConfig {
		Assertion.check()
				.isNotNull(apiClassOpt)
				.isNotNull(implClass)
				.isTrue(apiClassOpt.orElse(Component.class).isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, apiClassOpt.orElse(Component.class))
				.isNotNull(params)
				.when(apiClassOpt.isPresent(), () -> Assertion.check()
						.isTrue(Component.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must extend {1}", apiClassOpt, Component.class));
		//---
		params = List.copyOf(params);
	}

	/**
	 * Short in-line builder.
	 * @param apiClassOpt api of the component
	 * @param params params
	 */
	static ComponentConfig of(
			final Class<? extends Component> apiClass,
			final Class<? extends Component> implClass,
			final Param[] params) {
		return new ComponentConfig(Optional.of(apiClass), implClass, Arrays.asList(params));
	}

	/**
	 * Short in-line builder.
	 * @param implClass impl class of the component
	 * @param params params
	 */
	static ComponentConfig of(
			final Class<? extends Component> implClass,
			final Param[] params) {
		return new ComponentConfig(Optional.empty(), implClass, Arrays.asList(params));
	}
}
