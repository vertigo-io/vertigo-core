package io.vertigo.ccc;

import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.spaces.component.VCommand;
import io.vertigo.core.spaces.component.VCommandEngine;
import io.vertigo.core.spaces.component.VCommandExecutor;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.Option;

public class AAA {
	private final VCommandEngine myCommandEngine = null; // new VCommandEngineImpl(jsonEngine, VCommandEngine.DEFAULT_PORT); //Par défaut
	public AppConfigBuilder withCommandEngine(final VCommandEngine commandEngine) {
		Assertion.checkNotNull(commandEngine);
		Assertion.checkState(this.myCommandEngine == null, "commandEngine is already completed");
		//-----
		this.myCommandEngine = commandEngine;
		return this;
	}

	@JsonExclude
	private final Option<VCommandEngine> commandEngine;


	//VCommandEngine must be started after the container
	if (appConfig.getCommandEngine().isDefined()) {
		final VCommandEngine commandEngine = appConfig.getCommandEngine().get();
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
