package io.vertigo.studio.plugins.mda.file;

import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du FileInfoGenerator.
 * 
 * @author npiedeloup
 * @version $Id: FileInfoConfiguration.java,v 1.1 2013/07/11 10:04:04 npiedeloup Exp $
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
