package io.vertigo.studio.plugins.mda.task;

import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du Task Generator.
 * 
 * @author dchallas
 * @version $Id: TaskConfiguration.java,v 1.1 2013/07/11 10:04:04 npiedeloup Exp $
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
