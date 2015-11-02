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

import io.vertigo.core.component.AopEngine;
import io.vertigo.core.component.ElasticaEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Engine;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Option;
import io.vertigo.util.ListBuilder;

import java.util.List;

/**
 * This Class defines the properties of ComponentSpace and DefinitionSpace.
 * That's to say : how to boot the modules of Vertigo.
 * @author pchretien
 */
public final class BootConfig {
	private final Option<LogConfig> logConfigOption;
	private final boolean silence;
	@JsonExclude
	private final AopEngine aopEngine;
	@JsonExclude
	private final Option<ElasticaEngine> elasticaEngine;

	private final ModuleConfig bootModuleConfig;
	private final List<Engine> engines;

	/**
	 * Constructor.
	 * @param aopEngine AopEngine
	 * @param elasticaEngine ElasticaEngine (optional)
	 * @param silence is no logs
	 */
	BootConfig(
			final Option<LogConfig> logConfigOption,
			final ModuleConfig bootModuleConfig,
			final AopEngine aopEngine,
			final Option<ElasticaEngine> elasticaEngine,
			final boolean silence) {
		Assertion.checkNotNull(logConfigOption);
		Assertion.checkNotNull(bootModuleConfig);
		Assertion.checkNotNull(aopEngine);
		Assertion.checkNotNull(elasticaEngine);
		//-----
		this.logConfigOption = logConfigOption;
		this.bootModuleConfig = bootModuleConfig;
		this.silence = silence;
		this.aopEngine = aopEngine;
		this.elasticaEngine = elasticaEngine;
		//-----
		final ListBuilder<Engine> enginesBuilder = new ListBuilder<>();
		if (getElasticaEngine().isDefined()) {
			enginesBuilder.add(getElasticaEngine().get());
		}
		enginesBuilder.add(getAopEngine());

		engines = enginesBuilder.unmodifiable().build();
	}

	public Option<LogConfig> getLogConfig() {
		return logConfigOption;
	}

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
	public AopEngine getAopEngine() {
		return aopEngine;
	}

	/**
	 * @return Option of ElasticaEngine
	 */
	public Option<ElasticaEngine> getElasticaEngine() {
		return elasticaEngine;
	}

	public List<Engine> getEngines() {
		return engines;
	}
}
