package io.vertigo.core.config;

import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;

/**
 * Application state.
 */
public final class App implements AutoCloseable {
	private final AppConfig appConfig;

	/**
	 * Constructor.
	 * @param appConfig App Config
	 */
	public App(final AppConfig appConfig) {
		Assertion.checkNotNull(appConfig);
		//---------------------------------------------------------------------
		this.appConfig = appConfig;
		Home.start(appConfig);
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		Home.stop();
	}

	/**
	 * @return Environment Parameters
	 */
	public AppConfig getAppConfig() {
		return appConfig;
	}
}
