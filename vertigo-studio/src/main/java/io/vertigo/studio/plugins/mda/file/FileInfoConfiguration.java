package io.vertigo.studio.plugins.mda.file;

import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du FileInfoGenerator.
 * 
 * @author npiedeloup
 */
final class FileInfoConfiguration extends AbstractConfiguration {

	/**
	 * Constructeur.
	 * @param properties propriétes
	 */
	FileInfoConfiguration(final Properties properties) {
		super(properties);
	}

	String getFilePackage() {
		return getProjectPackageName() + ".fileinfo";
	}
}
