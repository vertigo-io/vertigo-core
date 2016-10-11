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
import io.vertigo.core.plugins.component.aop.cglib.CGLIBAopPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Configuration.
 *
 * @author npiedeloup, pchretien
 */
public final class BootConfigBuilder implements Builder<BootConfig> {
	private Optional<LogConfig> myLogConfigOption = Optional.empty(); //par défaut
	private final AppConfigBuilder appConfigBuilder;
	private boolean mySilence; //false by default
	private AopPlugin myAopPlugin = new CGLIBAopPlugin(); //By default
	private ModuleConfig myBootModuleConfig; //required

	/**
	 * @param appConfigBuilder Parent AppConfig builder
	 */
	BootConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		//-----
		this.appConfigBuilder = appConfigBuilder;
	}

	/**
	 * Ajout de paramètres
	 * @param logConfig Config of logs
	 * @return this builder
	 */
	public BootConfigBuilder withLogConfig(final LogConfig logConfig) {
		Assertion.checkNotNull(logConfig);
		//-----
		myLogConfigOption = Optional.of(logConfig);
		return this;
	}

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @return this builder
	 */
	public BootConfigBuilder silently() {
		mySilence = true;
		return this;
	}

	/**
	 * @return Module config builder
	 */
	ModuleConfigBuilder beginBootModule() {
		return new ModuleConfigBuilder(appConfigBuilder);
	}

	/**
	 * @param moduleConfig Module config
	 * @return this builder
	 */
	BootConfigBuilder withModule(final ModuleConfig moduleConfig) {
		Assertion.checkNotNull(moduleConfig);
		Assertion.checkState(myBootModuleConfig == null, "moduleConfig is already completed");
		//-----
		myBootModuleConfig = moduleConfig;
		return this;
	}

	/**
	 * @param aopPlugin AopPlugin
	 * @return this builder
	 */
	public BootConfigBuilder withAopEngine(final AopPlugin aopPlugin) {
		Assertion.checkNotNull(aopPlugin);
		//-----
		myAopPlugin = aopPlugin;
		return this;
	}

	/**
	 * @return AppConfig builder
	 */
	public AppConfigBuilder endBoot() {
		return appConfigBuilder;
	}

	/**
	 * @return BootConfig
	 */
	@Override
	public BootConfig build() {
		return new BootConfig(
				myLogConfigOption,
				myBootModuleConfig,
				myAopPlugin,
				mySilence);
	}
}
