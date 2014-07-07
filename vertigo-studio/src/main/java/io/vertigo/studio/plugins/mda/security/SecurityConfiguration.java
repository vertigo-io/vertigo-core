package io.vertigo.studio.plugins.mda.security;

import io.vertigo.studio.plugins.mda.AbstractConfiguration;

import java.util.Properties;

/**
 * Configuration du Security Generator.
 * 
 * @author dchallas
 * @version $Id: SecurityConfiguration.java,v 1.2 2014/03/07 09:31:19 pchretien Exp $
 */
final class SecurityConfiguration extends AbstractConfiguration {

	/**
	 * Constructeur.
	 * @param properties propri�tes
	 */
	SecurityConfiguration(final Properties properties) {
		super(properties);
	}

	String getSecurityPackage() {
		return getProjectPackageName() + ".security";
	}
}
