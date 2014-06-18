package io.vertigo.studio.plugins.mda.task;

import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du Task Generator.
 * 
 * @author dchallas
 */
final class TaskConfiguration extends AbstractConfiguration {
	/**
	 * Constructeur.
	 * @param properties propriétes
	 */
	TaskConfiguration(final Properties properties) {
		super(properties);
	}

	String getDaoPackage() {
		return getProjectPackageName() + ".dao";
	}
}
