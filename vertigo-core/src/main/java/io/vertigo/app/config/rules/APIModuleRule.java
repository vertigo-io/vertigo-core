/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app.config.rules;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.ModuleRule;
import io.vertigo.lang.VSystemException;

/**
 * Rule : all components of a module must have an API.
 *
 * @author pchretien
 */
public final class APIModuleRule implements ModuleRule {
	/** {@inheritDoc} */
	@Override
	public void check(final ModuleConfig moduleConfig) {
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			if (!componentConfig.getApiClass().isPresent()) {
				throw new VSystemException("api rule : all components of module '{0}' must have an api. Component '{1}' doesn't respect this rule.", moduleConfig, componentConfig);
			}
		}
	}
}
