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
import io.vertigo.engines.aop.cglib.CGLIBAopEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Option;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class BootConfigBuilder implements Builder<BootConfig> {
	private Option<LogConfig> myLogConfigOption = Option.none(); //par défaut
	private final AppConfigBuilder appConfigBuilder;
	private boolean mySilence; //false by default
	private AopEngine myAopEngine = new CGLIBAopEngine(); //By default
	private ElasticaEngine myElasticaEngine = null; //par défaut pas d'elasticité.
	private ModuleConfig myBootModuleConfig = null;

	BootConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		//-----
		this.appConfigBuilder = appConfigBuilder;
	}

	/**
	 * Ajout de paramètres
	 * @param logConfig Config of logs
	 */
	public BootConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		myLogConfigOption = Option.some(logConfig);
		return this;
	}

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @return Builder
	 */
	public BootConfigBuilder silently() {
		mySilence = true;
		return this;
	}

	ModuleConfigBuilder beginBootModule() {
		return new ModuleConfigBuilder(appConfigBuilder);
	}

	BootConfigBuilder withModule(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		Assertion.checkState(myBootModuleConfig == null, "moduleConfig is already completed");
		//-----
		myBootModuleConfig = moduleConfig;
		return this;
	}

	/**
	 * @param elasticaEngine ElasticaEngine
	 * @return this builder
	 */
	public BootConfigBuilder withElasticaEngine(final ElasticaEngine elasticaEngine) {
		Assertion.checkNotNull(elasticaEngine);
		Assertion.checkState(myElasticaEngine == null, "elasticaEngine is already completed");
		//-----
		myElasticaEngine = elasticaEngine;
		return this;
	}

	/**
	 * @param aopEngine AopEngine
	 * @return this builder
	 */
	public BootConfigBuilder withAopEngine(final AopEngine aopEngine) {
		Assertion.checkNotNull(aopEngine);
		//-----
		myAopEngine = aopEngine;
		return this;
	}

	public AppConfigBuilder endBoot() {
		return appConfigBuilder;
	}

	/**
	 * @return BootConfig
	 */
	@Override
	public BootConfig build() {
		if (myBootModuleConfig == null) {
			beginBootModule().endModule();
		}
		return new BootConfig(
				myLogConfigOption,
				myBootModuleConfig,
				myAopEngine,
				Option.option(myElasticaEngine),
				mySilence);
	}
}
