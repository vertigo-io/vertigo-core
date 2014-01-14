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

import io.vertigo.kernel.engines.AopEngine;
import io.vertigo.kernel.engines.ElasticaEngine;
import io.vertigo.kernel.engines.JsonEngine;
import io.vertigo.kernel.engines.RestEngine;
import io.vertigo.kernel.engines.VCommandEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.lang.Loader;
import io.vertigo.kernel.lang.Option;
import io.vertigoimpl.engines.aop.cglib.CGLIBAopEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration.
 * 
 * @author npiedeloup, pchretien
 */
public final class ComponentSpaceConfigBuilder implements Builder<ComponentSpaceConfig> {
	private final List<ModuleConfigBuilder> moduleConfigBuilders = new ArrayList<>();
	private final Map<String, String> params = new HashMap<>(); //par défaut vide
	private boolean silence;
	private AopEngine aopEngine = new CGLIBAopEngine();
	private JsonEngine jsonEngine = null;
	private RestEngine restEngine = null; //par défaut par de serveur 
	private ElasticaEngine elasticaEngine = null; //par défaut pas d'elasticité.
	private VCommandEngine commandEngine = null; // new VCommandEngineImpl(jsonEngine, VCommandEngine.DEFAULT_PORT); //Par défaut

	//=========================================================================
	//==================Paramétrage général====================================
	//=========================================================================
	/**
	 * Ajout de paramètres  
	 * @param paramName Nom du paramètre
	 * @param paramValue Valeur du paramètre
	 * @return
	 */
	public ComponentSpaceConfigBuilder withParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName);
		Assertion.checkNotNull(paramValue);
		//---------------------------------------------------------------------
		params.put(paramName, paramValue);
		return this;
	}

	/**
	 * Permet d'externaliser le processus de chargement dans un système dédié
	 * @param loader Responsable du chagement d'un fragment de la conf
	 * @return Builder
	 */
	public ComponentSpaceConfigBuilder withLoader(final Loader<ComponentSpaceConfigBuilder> loader) {
		loader.load(this);
		return this;
	}

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @param newSilence Si le mode est silencieux 
	 * @return Builder
	 */
	public ComponentSpaceConfigBuilder withSilence(final boolean newSilence) {
		silence = newSilence;
		return this;
	}

	public ComponentSpaceConfigBuilder withRestEngine(final RestEngine newRestEngine) {
		Assertion.checkNotNull(newRestEngine);
		Assertion.checkState(restEngine == null, "restEngine is alreday completed");
		//---------------------------------------------------------------------
		restEngine = newRestEngine;
		return this;
	}

	public ComponentSpaceConfigBuilder withJsonEngine(final JsonEngine newJsonEngine) {
		Assertion.checkNotNull(newJsonEngine);
		//---------------------------------------------------------------------
		jsonEngine = newJsonEngine;
		return this;
	}

	public ComponentSpaceConfigBuilder withCommandEngine(final VCommandEngine newCommandEngine) {
		Assertion.checkNotNull(newCommandEngine);
		//---------------------------------------------------------------------
		commandEngine = newCommandEngine;
		return this;
	}

	public ComponentSpaceConfigBuilder withElasticaEngine(final ElasticaEngine newElasticaEngine) {
		Assertion.checkNotNull(newElasticaEngine);
		Assertion.checkState(elasticaEngine == null, "elasticaEngine is alreday completed");
		//---------------------------------------------------------------------
		elasticaEngine = newElasticaEngine;
		return this;
	}

	public ComponentSpaceConfigBuilder withAopEngine(final AopEngine newAopEngine) {
		Assertion.checkNotNull(newAopEngine);
		//---------------------------------------------------------------------
		aopEngine = newAopEngine;
		return this;
	}

	//=========================================================================
	//==============================Module=====================================
	//=========================================================================
	/**
	 * Ajout d'un module
	 * @param name Nom du module
	 * @return Builder
	 */
	public ModuleConfigBuilder beginModule(final String name) {
		//On remet à null le plugin et le composant courant
		final ModuleConfigBuilder moduleConfigBuilder = new ModuleConfigBuilder(this, name);
		moduleConfigBuilders.add(moduleConfigBuilder);
		return moduleConfigBuilder;
	}

	//=========================================================================
	//==============================Builder=====================================
	//=========================================================================

	/** {@inheritDoc} */
	public ComponentSpaceConfig build() {
		final List<ModuleConfig> moduleConfigs = new ArrayList<>();
		for (final ModuleConfigBuilder moduleConfigBuilder : moduleConfigBuilders) {
			final ModuleConfig moduleConfig = moduleConfigBuilder.build();
			moduleConfigs.add(moduleConfig);
		}
		return new ComponentSpaceConfig(params, moduleConfigs, aopEngine, Option.option(elasticaEngine), Option.option(restEngine), Option.option(commandEngine), Option.option(jsonEngine), silence);
	}
}
