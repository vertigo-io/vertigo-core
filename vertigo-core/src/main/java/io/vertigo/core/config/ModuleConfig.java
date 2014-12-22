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
package io.vertigo.core.config;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration of a module.
 * This config module contains
 *  - config of components
 *  - config of plugins
 *  - config of resources
 *  - params
 *  - rules
 *
 * @author npiedeloup, pchretien
 */
public final class ModuleConfig {
	private final String name;
	private final List<ResourceConfig> resources;
	private final List<ComponentConfig> components;
	private final List<AspectConfig> aspects;
	@JsonExclude
	private final List<ModuleRule> moduleRules;

	ModuleConfig(final String name, final List<ResourceConfig> resourceConfigs, final List<ComponentConfig> componentConfigs, final List<AspectConfig> aspectConfigs, final List<ModuleRule> moduleRules) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(resourceConfigs);
		Assertion.checkNotNull(componentConfigs);
		Assertion.checkNotNull(aspectConfigs);
		Assertion.checkNotNull(moduleRules);
		//-----
		this.name = name;
		this.resources = Collections.unmodifiableList(new ArrayList<>(resourceConfigs));
		this.components = Collections.unmodifiableList(new ArrayList<>(componentConfigs));
		this.aspects = aspectConfigs;
		this.moduleRules = Collections.unmodifiableList(new ArrayList<>(moduleRules));
	}

	public List<ResourceConfig> getResourceConfigs() {
		return resources;
	}

	/**
	 * @return Liste des configurations de composants.
	 */
	public List<ComponentConfig> getComponentConfigs() {
		return components;
	}

	public List<AspectConfig> getAspectConfigs() {
		return aspects;
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
