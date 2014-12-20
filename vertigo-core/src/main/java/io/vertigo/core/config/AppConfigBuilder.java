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

import io.vertigo.core.engines.AopEngine;
import io.vertigo.core.engines.ElasticaEngine;
import io.vertigo.core.engines.VCommandEngine;
import io.vertigo.engines.aop.cglib.CGLIBAopEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Loader;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class AppConfigBuilder implements Builder<AppConfig> {
	private final List<ModuleConfigBuilder> myModuleConfigBuilders = new ArrayList<>();
	private final Map<String, String> myParams = new HashMap<>(); //par défaut vide
	private boolean mySilence;
	private AopEngine myAopEngine = new CGLIBAopEngine();
	private ElasticaEngine myElasticaEngine = null; //par défaut pas d'elasticité.
	private VCommandEngine myCommandEngine = null; // new VCommandEngineImpl(jsonEngine, VCommandEngine.DEFAULT_PORT); //Par défaut

	/**
	 * Ajout de paramètres
	 * @param paramName Nom du paramètre
	 * @param paramValue Valeur du paramètre
	 */
	public AppConfigBuilder withParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName);
		Assertion.checkNotNull(paramValue);
		//---------------------------------------------------------------------
		myParams.put(paramName, paramValue);
		return this;
	}

	/**
	 * Permet d'externaliser le processus de chargement dans un système dédié
	 * @param loader Responsable du chagement d'un fragment de la conf
	 * @return Builder
	 */
	public AppConfigBuilder withLoader(final Loader<AppConfigBuilder> loader) {
		loader.load(this);
		return this;
	}

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @param silence Si le mode est silencieux
	 * @return Builder
	 */
	public AppConfigBuilder withSilence(final boolean silence) {
		this.mySilence = silence;
		return this;
	}

	public AppConfigBuilder withCommandEngine(final VCommandEngine commandEngine) {
		Assertion.checkNotNull(commandEngine);
		//---------------------------------------------------------------------
		this.myCommandEngine = commandEngine;
		return this;
	}

	public AppConfigBuilder withElasticaEngine(final ElasticaEngine elasticaEngine) {
		Assertion.checkNotNull(elasticaEngine);
		Assertion.checkState(this.myElasticaEngine == null, "elasticaEngine is alreday completed");
		//---------------------------------------------------------------------
		this.myElasticaEngine = elasticaEngine;
		return this;
	}

	public AppConfigBuilder withAopEngine(final AopEngine aopEngine) {
		Assertion.checkNotNull(aopEngine);
		//---------------------------------------------------------------------
		this.myAopEngine = aopEngine;
		return this;
	}

	/**
	 * Ajout d'un module
	 * @param name Nom du module
	 * @return Builder
	 */
	public ModuleConfigBuilder beginModule(final String name) {
		//On remet à null le plugin et le composant courant
		final ModuleConfigBuilder moduleConfigBuilder = new ModuleConfigBuilder(this, name);
		myModuleConfigBuilders.add(moduleConfigBuilder);
		return moduleConfigBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public AppConfig build() {
		final List<ModuleConfig> moduleConfigs = new ArrayList<>();
		for (final ModuleConfigBuilder moduleConfigBuilder : myModuleConfigBuilders) {
			final ModuleConfig moduleConfig = moduleConfigBuilder.build();
			moduleConfigs.add(moduleConfig);
		}
		return new AppConfig(myParams, moduleConfigs, myAopEngine, Option.option(myElasticaEngine), Option.option(myCommandEngine), mySilence);
	}
}
