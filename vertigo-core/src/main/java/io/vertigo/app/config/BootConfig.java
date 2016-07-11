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

import java.util.Optional;

import io.vertigo.core.component.AopPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;

/**
 * This Class defines the properties of ComponentSpace and DefinitionSpace.
 * That's to say : how to boot the modules of Vertigo.
 * @author pchretien
 */
public final class BootConfig {
	private final Optional<LogConfig> logConfigOption;
	private final boolean silence;
	@JsonExclude
	private final AopPlugin aopPlugin;

	private final ModuleConfig bootModuleConfig;

	/**
	 * Constructor.
	 * @param aopPlugin AopPlugin
	 * @param silence is no logs
	 */
	BootConfig(
			final Optional<LogConfig> logConfigOption,
			final ModuleConfig bootModuleConfig,
			final AopPlugin aopPlugin,
			final boolean silence) {
		Assertion.checkNotNull(logConfigOption);
		Assertion.checkNotNull(bootModuleConfig);
		Assertion.checkNotNull(aopPlugin);
		//-----
		this.logConfigOption = logConfigOption;
		this.bootModuleConfig = bootModuleConfig;
		this.silence = silence;
		this.aopPlugin = aopPlugin;
	}

	/**
	 * @return the logconfig
	 */
	public Optional<LogConfig> getLogConfig() {
		return logConfigOption;
	}

	/**
	 *
	 * @return the config of the boot consireded as a module
	 */
	public ModuleConfig getBootModuleConfig() {
		return bootModuleConfig;
	}

	/**
	 * @return if silent mode
	 */
	public boolean isSilence() {
		return silence;
	}

	/**
	 * @return AopEngine
	 */
	public AopPlugin getAopPlugin() {
		return aopPlugin;
	}
}
