package io.vertigo.rest.plugins.rest.servlet;

import io.vertigo.commons.impl.config.ConfigPlugin;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Plugin d'accès à la configuration de la WebApp.
 * @author npiedeloup
 */
public final class WebAppContextConfigPlugin implements ConfigPlugin {
	private static Properties properties;

	/**
	 * @param initConf Configuration initiale
	 */
	public static void setInitConfig(final Properties initConf) {
		Assertion.checkNotNull(initConf);
		//---------------------------------------------------------------------
		WebAppContextConfigPlugin.properties = initConf;
	}

	private final String managedConfigPath;

	/**
	 * Constructeur.
	 * @param configPath Nom de la config initial
	 */
	@Inject
	public WebAppContextConfigPlugin(@Named("configPath") final String configPath) {
		Assertion.checkArgNotEmpty(configPath);
		// ---------------------------------------------------------------------
		managedConfigPath = configPath;
	}

	/** {@inheritDoc} */
	@Override
	public Option<String> getValue(final String configPath, final String property) {
		Assertion.checkArgNotEmpty(configPath);
		Assertion.checkArgNotEmpty(property);
		//---------------------------------------------------------------------
		return managedConfigPath.equals(configPath) ? Option.<String> option(properties.getProperty(property)) : Option.<String> none();
	}
}
