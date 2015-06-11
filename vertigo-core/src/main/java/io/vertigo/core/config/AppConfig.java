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

import io.vertigo.core.boot.BootConfig;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final Option<LogConfig> logConfigOption;
	private final BootConfig bootConfig;
	private final List<ModuleConfig> modules;

	AppConfig(
			final Option<LogConfig> logConfigOption,
			final BootConfig bootConfig,
			final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(logConfigOption);
		Assertion.checkNotNull(bootConfig);
		Assertion.checkNotNull(moduleConfigs);
		//---
		this.logConfigOption = logConfigOption;
		this.bootConfig = bootConfig;
		this.modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
	}

	public BootConfig getBootConfig() {
		return bootConfig;
	}

	public Option<LogConfig> getLogConfig() {
		return logConfigOption;
	}

	/**
	 * @return Liste des configurations de modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}
}
