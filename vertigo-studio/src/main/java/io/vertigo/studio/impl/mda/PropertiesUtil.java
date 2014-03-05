package io.vertigo.studio.impl.mda;

import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.xml.XMLModulesLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Helper de lecture d'un fichier de propriétés.
 * 
 * @author dchallas 
 * @version $Id: PropertiesUtil.java,v 1.8 2013/12/04 13:34:50 pchretien Exp $
 */
public final class PropertiesUtil {

	private PropertiesUtil() {
		// rien
	}

	/**
	 * @param params Paramètres de l'application (environnement, webapp, ...)
	 */
	public static ComponentSpaceConfig parse(final Properties params, final boolean silence) {
		final String[] xmlFileNames = params.getProperty("applicationConfiguration").split(";");
		Assertion.checkArgument(xmlFileNames.length >= 1, "Il faut au moins une configuration");
		//---------------------------------------------------------------------
		final List<XMLModulesLoader> moduleLoaders = new ArrayList<>();
		for (final String xmlFileName : xmlFileNames) {
			final URL xmlUrl = PropertiesUtil.createURL(xmlFileName);
			moduleLoaders.add(new XMLModulesLoader(xmlUrl, params));
		}
		final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder() //
				.withSilence(silence); //
		//.withRestEngine(new GrizzlyRestEngine(8086));
		for (final XMLModulesLoader modulesLoader : moduleLoaders) {
			componentSpaceConfigBuilder.withLoader(modulesLoader);
		}
		return componentSpaceConfigBuilder.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 * 
	 * @param fileName Nom du fichier
	 * @return URN non null
	 */
	public static URL createURL(final String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		//---------------------------------------------------------------------
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath 
			final URL url = PropertiesUtil.class.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	/**
	 * Retourne une propriété non null.
	 * @param properties Propriétés
	 * @param propertyName Nom de la propriété recherchée
	 * @param messageIfNull Message en cas de propriété non trouvée
	 * @return Valeur de la propriété
	 */
	public static String getPropertyNotNull(final Properties properties, final String propertyName, final String messageIfNull) {
		Assertion.checkNotNull(properties);
		Assertion.checkNotNull(propertyName);
		//---------------------------------------------------------------------
		final String property = properties.getProperty(propertyName, null);
		Assertion.checkNotNull(property, messageIfNull);
		return property.trim();
	}
}
