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
package io.vertigo.core.di.configurator;

import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.lang.Option;
import io.vertigo.kernel.engines.AopEngine;
import io.vertigo.kernel.engines.ElasticaEngine;
import io.vertigo.kernel.engines.VCommandEngine;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Paramétrage des composants de l'application.
 * 
 * @author npiedeloup, pchretien
 */
public final class ComponentSpaceConfig {
	private final List<ModuleConfig> modules;
	private final Map<String, String> params;
	private final boolean silence;
	@JsonExclude
	private final AopEngine aopEngine;
	@JsonExclude
	private final Option<ElasticaEngine> elasticaEngine;
	@JsonExclude
	private final Option<VCommandEngine> commandEngine;

	ComponentSpaceConfig(final Map<String, String> params, final List<ModuleConfig> moduleConfigs, final AopEngine aopEngine, final Option<ElasticaEngine> elasticaEngine, final Option<VCommandEngine> commandEngine, final boolean silence) {
		Assertion.checkNotNull(params);
		Assertion.checkNotNull(moduleConfigs);
		//---
		Assertion.checkNotNull(aopEngine);
		Assertion.checkNotNull(elasticaEngine);
		Assertion.checkNotNull(commandEngine);
		//---------------------------------------------------------------------
		this.params = params;
		this.modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
		this.silence = silence;
		this.aopEngine = aopEngine;
		this.elasticaEngine = elasticaEngine;
		this.commandEngine = commandEngine;
	}

	/**
	 * @return Liste des configurations de modules
	 */
	List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	/**
	 * @return Map des paramètres globaux
	 */
	Map<String, String> getParams() {
		return params;
	}

	boolean isSilence() {
		return silence;
	}

	AopEngine getAopEngine() {
		return aopEngine;
	}

	Option<VCommandEngine> getCommandEngine() {
		return commandEngine;
	}

	Option<ElasticaEngine> getElasticaEngine() {
		return elasticaEngine;
	}
}
