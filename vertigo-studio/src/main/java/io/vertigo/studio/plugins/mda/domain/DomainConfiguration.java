package io.vertigo.studio.plugins.mda.domain;

import static io.vertigo.studio.impl.mda.PropertiesUtil.getPropertyNotNull;
import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du DomainGenerator.
 * 
 * @author dchallas
 * @version $Id: DomainConfiguration.java,v 1.1 2013/07/11 10:04:05 npiedeloup Exp $
 */
final class DomainConfiguration extends AbstractConfiguration {
	private final boolean generateResourcesFile;

	/**
	 * Constructeur.
	 * @param properties propriétes
	 */
	DomainConfiguration(final Properties properties) {
		super(properties);
		//---------------------------------------------------------------------
		generateResourcesFile = Boolean.parseBoolean(getPropertyNotNull(properties, "generateResourcesFile", "generateResourcesFile doit être renseigné à true ou false"));
	}

	boolean isResourcesFileGenerated() {
		return generateResourcesFile;
	}

	String getDomainPackage() {
		return getProjectPackageName() + ".domain";
	}
}
