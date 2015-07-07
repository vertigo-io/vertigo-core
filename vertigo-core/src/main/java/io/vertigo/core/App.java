package io.vertigo.core;

import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.spaces.component.ComponentLoader;
import io.vertigo.core.spaces.component.ComponentSpace;
import io.vertigo.core.spaces.config.ConfigSpace;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.dynamo.impl.environment.DefinitionLoader;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Engine;
import io.vertigo.util.StringUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public final class App implements AutoCloseable {
	private static enum State {
		/** Composants en cours de démarrage*/
		starting,
		/** Composants configurés et démarrés*/
		active,
		/** Composants en cours d'arrét*/
		stopping,
		/** Terminé*/
		closed
	}

	private static Logger LOGGER = Logger.getLogger(App.class);

	//Start Date in milliseconds : used to have 'uptime'
	private final long start;
	private final AppConfig appConfig;
	private State state;

	private final DefinitionSpace definitionSpace;
	private final ComponentSpace componentSpace;
	private final ConfigSpace configSpace;

	/**
	 * Constructor.
	 * @param appConfig Application configuration
	 */
	public App(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//-----
		start = System.currentTimeMillis();
		this.appConfig = appConfig;
		Home.setApp(this);
		state = State.starting;
		//-----
		try {
			if (appConfig.getLogConfig().isDefined()) {
				initLog(appConfig.getLogConfig().get());
			}
			//-----
			configSpace = new ConfigSpace();
			definitionSpace = new DefinitionSpace();
			componentSpace = new ComponentSpace(appConfig.getBootConfig().isSilence());

			final ComponentLoader componentLoader = new ComponentLoader(appConfig.getBootConfig(), componentSpace);

			//-----0. Boot (considered as a Module)
			componentLoader.injectComponent(appConfig.getBootConfig().getBootModuleConfig());

			//-----1. Load all definitions
			final String EnvironmentManagerId = StringUtil.first2LowerCase(EnvironmentManager.class.getSimpleName());
			if (componentSpace.contains(EnvironmentManagerId)) {
				final EnvironmentManager environmentManager = componentSpace.resolve(EnvironmentManager.class);
				final DefinitionLoader definitionLoader = environmentManager.createDefinitionLoader();
				definitionLoader.injectDefinitions(appConfig.getModuleConfigs());
			}
			//-----2. Load all components (and aspects).
			componentLoader.injectComponents(appConfig.getModuleConfigs());
			//-----
			startEngines();
			componentSpace.start();
			definitionSpace.start();
			state = State.active;
		} catch (final Exception e) {
			close();
			throw new RuntimeException("an error occured when starting", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		//En cas d'erreur on essaie de fermer proprement les composants démarrés.
		Assertion.checkState(state == State.active || state == State.starting, "App with a state '{0}' can not be be closed", state);
		state = State.stopping;
		//-----
		try {
			definitionSpace.stop();
			componentSpace.stop();
			stopEngines();
		} catch (final Exception e) {
			LOGGER.error("an error occured when stopping", e);
			//Quel que soit l'état, on part en échec de l'arrét.
			throw new RuntimeException("an error occured when stopping", e);
		} finally {
			state = State.closed;
			Home.setApp(null);
		}
	}

	private void startEngines() {
		for (final Engine engine : appConfig.getBootConfig().getEngines()) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).start();
			}
		}
	}

	private void stopEngines() {
		final List<Engine> reverseEngines = new ArrayList<>(appConfig.getBootConfig().getEngines());
		java.util.Collections.reverse(reverseEngines);

		for (final Engine engine : reverseEngines) {
			if (engine instanceof Activeable) {
				Activeable.class.cast(engine).stop();
			}
		}
	}

	/**
	 * @return Start Date in milliseconds
	 */
	public long getStartDate() {
		return start;
	}

	/**
	 * @return Application configuration
	 */
	public AppConfig getConfig() {
		return appConfig;
	}

	DefinitionSpace getDefinitionSpace() {
		return definitionSpace;
	}

	ComponentSpace getComponentSpace() {
		return componentSpace;
	}

	ConfigSpace getConfigSpace() {
		return configSpace;
	}

	private static void initLog(final LogConfig log4Config) {
		Assertion.checkNotNull(log4Config);
		//-----
		final String log4jFileName = log4Config.getFileName();
		Assertion.checkArgument(log4jFileName.endsWith(".xml"), "Use the XML format for log4j configurations (instead of : {0}).", log4jFileName);
		final URL url = Home.class.getResource(log4jFileName);
		if (url != null) {
			DOMConfigurator.configure(url);
			Logger.getRootLogger().info("Log4J configuration chargée (resource) : " + url.getFile());
		} else {
			Assertion.checkArgument(new File(log4jFileName).exists(), "Fichier de configuration log4j : {0} est introuvable", log4jFileName);
			// Avec configureAndWatch (utilise un anonymous thread)
			// on peut modifier à chaud le fichier de conf log4j
			// mais en cas de hot-deploy, le thread reste présent ce qui peut-entrainer des problèmes.
			DOMConfigurator.configureAndWatch(log4jFileName);
		}
		Logger.getRootLogger().info("Log4J configuration chargée (fichier) : " + log4jFileName);
	}
}
