package io.vertigo.core.config;

import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;

import java.util.Properties;

/**
 * Application state.
 */
public final class App implements AutoCloseable {
	private final Properties envParams;

	/**
	 * Constructor.
	 * @param envParams Environment Parameters (are public in app)
	 * @param appConfig App Config
	 */
	App(final Properties envParams, final AppConfig appConfig) {
		Assertion.checkNotNull(envParams, "envParams");
		//---------------------------------------------------------------------
		this.envParams = envParams;
		Home.start(appConfig);
	}

	/** {@inheritDoc} */
	public void close() {
		Home.stop();
	}

	/**
	 * @return Environment Parameters
	 */
	public Properties getEnvParams() {
		return envParams;
	}
}
