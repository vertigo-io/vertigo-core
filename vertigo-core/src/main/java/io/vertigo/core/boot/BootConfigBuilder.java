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
package io.vertigo.core.boot;

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
	private boolean mySilence; //false by default
	private AopEngine myAopEngine = new CGLIBAopEngine(); //By default
	private ElasticaEngine myElasticaEngine = null; //par défaut pas d'elasticité.

	/**
	 * Permet de définir un démarrage silencieux. (Sans retour console)
	 * @return Builder
	 */
	public BootConfigBuilder silently() {
		this.mySilence = true;
		return this;
	}

	public BootConfigBuilder withElasticaEngine(final ElasticaEngine elasticaEngine) {
		Assertion.checkNotNull(elasticaEngine);
		Assertion.checkState(this.myElasticaEngine == null, "elasticaEngine is already completed");
		//-----
		this.myElasticaEngine = elasticaEngine;
		return this;
	}

	public BootConfigBuilder withAopEngine(final AopEngine aopEngine) {
		Assertion.checkNotNull(aopEngine);
		//-----
		this.myAopEngine = aopEngine;
		return this;
	}

	/**
	 * @return BootConfig
	 */
	@Override
	public BootConfig build() {
		return new BootConfig(
				myAopEngine,
				Option.option(myElasticaEngine),
				mySilence);
	}
}
