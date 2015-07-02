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
package io.vertigo.ccc;

import io.vertigo.core.Home;
import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VCommandExecutor;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.engines.VCommandEngine;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

public class AAA {

	//	private final VCommandEngine myCommandEngine = null; // new VCommandEngineImpl(jsonEngine, VCommandEngine.DEFAULT_PORT); //Par défaut

	public void start(final AppConfig appConfig, final VCommandEngine commandEngine) {
		//	public final AppConfigBuilder withCommandEngine(final VCommandEngine commandEngine) {
		//		Assertion.checkNotNull(commandEngine);
		//		Assertion.checkState(this.myCommandEngine == null, "commandEngine is already completed");
		//		//-----
		//		this.myCommandEngine = commandEngine;
		//		return this;
		//	}

		//		@JsonExclude
		//		private final Option<VCommandEngine> commandEngine;

		//VCommandEngine must be started after the container
		if (commandEngine instanceof Activeable) {
			((Activeable) commandEngine).start();
		}
		//			engines.add(commandEngine);
		//		}
		//
		//		if (componentSpaceConfig.getCommandEngine().isDefined()) {
		commandEngine.registerCommandExecutor("config", new VCommandExecutor<AppConfig>() {
			@Override
			public AppConfig exec(final VCommand command) {
				return appConfig;
			}
		});

		commandEngine.registerCommandExecutor("definitions", new VCommandExecutor<DefinitionSpace>() {
			/** {@inheritDoc} */
			@Override
			public DefinitionSpace exec(final VCommand command) {
				Assertion.checkNotNull(command);
				//-----
				return Home.getDefinitionSpace();
			}
		});
	}

}
