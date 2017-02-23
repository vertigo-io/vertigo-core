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

import io.vertigo.core.component.ComponentInitializer;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.util.ListBuilder;

/**
 * The AppConfigBuilder builder allows you to create an AppConfig using a fluent, simple style .
 *
 * @author npiedeloup, pchretien
 */
public final class AppConfigBuilder implements Builder<AppConfig> {
	private final ListBuilder<ModuleConfig> myModuleConfigListBuilder = new ListBuilder<>();
	private final BootConfigBuilder myBootConfigBuilder;
	private final List<ComponentInitializerConfig> myComponentInitializerConfigs = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public AppConfigBuilder() {
		myBootConfigBuilder = new BootConfigBuilder(this);

	}

	/**
	 * Opens the bootConfigBuilder.
	 * There is exactly one BootConfig per AppConfig.
	 * @return this builder
	 */
	public BootConfigBuilder beginBoot() {
		return myBootConfigBuilder;
	}

	/**
	 * Adds an initializer to the current config.
	 * @param componentInitializerClass Class of the initializer
	 * @return this builder
	 */
	public AppConfigBuilder addInitializer(final Class<? extends ComponentInitializer> componentInitializerClass) {
		myComponentInitializerConfigs.add(new ComponentInitializerConfig(componentInitializerClass));
		return this;
	}

	/**
	 * Adds a a moduleConfig.
	 * @param moduleConfig the moduleConfig
	 * @return this builder
	 */
	public AppConfigBuilder addModule(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		//-----
		myModuleConfigListBuilder.add(moduleConfig);
		return this;
	}

	/**
	 * Builds the appConfig.
	 * @return appConfig.
	 */
	@Override
	public AppConfig build() {
		return new AppConfig(myBootConfigBuilder.build(),
				myModuleConfigListBuilder.unmodifiable().build(),
				myComponentInitializerConfigs);
	}
}
