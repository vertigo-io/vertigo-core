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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.amplifier.AmplifierMethod;

/**
 * The ProxyMethodConfig class defines the way to create a new proxy on an interface using small proxy methods.
 *
 * this class is composed of
 *  - an interface identified by an annotation
 *  - a proxy method used to build a dynamic the proxy
 *
 * @author pchretien
 *
 * @papram proxyMethodClass the proxy method class
 */
public record AmplifierMethodConfig(
		Class<? extends AmplifierMethod> amplifierMethodClass) {

	public AmplifierMethodConfig {
		Assertion.check().isNotNull(amplifierMethodClass);
	}
}
