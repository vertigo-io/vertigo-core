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
package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.JsonExclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Configuration of a module.
 * This config module contains 
 *  - config of components
 *  - config of plugins
 *  - params
 *  - rules
 * 
 * @author npiedeloup, pchretien
 */
final class ModuleConfig {
	private final String name;
	private final List<ComponentConfig> componentConfigs;
	private final List<AspectConfig> aspectConfigs;
	@JsonExclude
	private final List<ModuleRule> moduleRules;

	ModuleConfig(final String name, final List<ComponentConfig> componentConfigs, final List<AspectConfig> aspectConfigs, final List<ModuleRule> moduleRules) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(componentConfigs);
		Assertion.checkNotNull(aspectConfigs);
		Assertion.checkNotNull(moduleRules);
		//---------------------------------------------------------------------
		this.name = name;
		this.componentConfigs = Collections.unmodifiableList(new ArrayList<>(componentConfigs));
		this.aspectConfigs = aspectConfigs;
		this.moduleRules = Collections.unmodifiableList(new ArrayList<>(moduleRules));
	}

	/**
	 * @return Liste des configurations de composants.
	 */
	List<ComponentConfig> getComponentConfigs() {
		return componentConfigs;
	}

	List<AspectConfig> getAspectConfigs() {
		return aspectConfigs;
	}

	/**
	 * @return Nom du module.
	 */
	String getName() {
		return name;
	}

	void checkRules() {
		for (final ModuleRule moduleRule : moduleRules) {
			moduleRule.chek(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public String toString() {
		return name;
	}
}
