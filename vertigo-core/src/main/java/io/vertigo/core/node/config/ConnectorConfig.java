/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2021, Vertigo.io, team@vertigo.io
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

import java.util.List;
import java.util.Optional;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.param.Param;

/**
 * This class defines the configuration of a connector.
 *
 * A connector is defined by
 *  - an api class
 *  - a implementation class
 *  - a list of params
 *
 * The same connector can be used many times with distincts params
 *  - for example : two connections to differents timeseries databases
 *
 * @author mlaroche
 * 
 * @param apiClassOpt the api class of the connector
 * @param implClass the impl class of the connector
 * @param params the params
 */
public final record ConnectorConfig(
		Optional<Class<? extends Connector>> apiClassOpt,
		Class<? extends Connector> implClass,
		List<Param> params) {

	public ConnectorConfig {
		Assertion.check()
				.isNotNull(apiClassOpt)
				.isNotNull(implClass)
				.isTrue(Connector.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Connector.class)
				.isNotNull(params)
				.when(apiClassOpt.isPresent(), () -> Assertion.check()
						.isTrue(Connector.class.isAssignableFrom(apiClassOpt.get()), "api class {0} must implement {1}", apiClassOpt, Connector.class)
						.isTrue(apiClassOpt.get().isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, apiClassOpt)
						.isTrue(apiClassOpt.get().isInterface(), "api class {0} must be an interface", apiClassOpt));
		//---
		params = List.copyOf(params);
	}
}
