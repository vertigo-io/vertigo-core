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
import io.vertigo.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class AppConfigBuilder implements Builder<AppConfig> {
	private final List<ModuleConfig> myModuleConfigs = new ArrayList<>();
	private final BootConfigBuilder myBootConfigBuilder = new BootConfigBuilder(this);

	//There is exactly one BootConfig(Builder) per AppConfig(Builer).  

	public ModuleConfigBuilder beginBootModule() {
		return myBootConfigBuilder.beginBootModule().withNoAPI();
	}

	public BootConfigBuilder beginBoot() {
		return myBootConfigBuilder;
	}

	/**
	 * Permet d'externaliser le processus de chargement dans un système dédié
	 * @param moduleConfigs Liste des modules
	 * @return Builder
	 */
	public AppConfigBuilder withModules(final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		myModuleConfigs.addAll(moduleConfigs);
		return this;
	}

	/**
	 * Ajout d'un module
	 * @param name Nom du module
	 * @return Builder
	 */
	public ModuleConfigBuilder beginModule(final String name) {
		return new ModuleConfigBuilder(this, name);
	}

	/**
	 * Update the 'already set' componentSpaceConfigBuilder and return it.
	 * @return ComponentSpaceConfigBuilder
	 */
	@Override
	public AppConfig build() {
		return new AppConfig(myBootConfigBuilder.build(), myModuleConfigs);
	}
}
