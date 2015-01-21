package io.vertigo.boot.xml;

import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class XMLAppConfigBuilder implements Builder<AppConfig> {
	private boolean mySilence;

	private final Properties myEnvParams = new Properties();
	private final List<URL> xmlUrls = new ArrayList<>();

	/**
	 * Append EnvParams.
	 * @param envParams envParams
	 * @return this builder
	 */
	public XMLAppConfigBuilder withEnvParams(final Properties envParams) {
		Assertion.checkNotNull(envParams);
		//-----
		myEnvParams.putAll(envParams);
		return this;
	}

	/**
	 * Append XmlFiles.
	 * @param relativeRootClass Class use for relative path
	 * @param xmlFileNames Multiple xmlFileName
	 * @return this builder
	 */
	public XMLAppConfigBuilder withXmlFileNames(final Class<?> relativeRootClass, final String... xmlFileNames) {
		for (final String xmlFileName : xmlFileNames) {
			final URL xmlUrl = createURL(xmlFileName, relativeRootClass);
			xmlUrls.add(xmlUrl);
		}
		return this;
	}

	public XMLAppConfigBuilder withSilence(final boolean silence) {
		mySilence = silence;
		return this;
	}

	@Override
	public AppConfig build() {
		//1- if no xmlUrls we check if a property reference files
		final String xmlFileNames = myEnvParams.getProperty("boot.applicationConfiguration");
		if (xmlFileNames != null) {
			final String[] xmlFileNamesSplit = xmlFileNames.split(";");
			withXmlFileNames(getClass(), xmlFileNamesSplit);
			//---
			myEnvParams.remove("boot.applicationConfiguration");
		}
		//-----
		Assertion.checkNotNull(xmlUrls, "No config found");
		//-----
		//2- We load XML with parser to obtain all the moduleConfigs
		// We check that all the properties are consumed.
		return new AppConfigBuilder()
				.withSilence(mySilence)
				.withModules(XMLModulesParser.parseAll(myEnvParams, xmlUrls))
				.build();
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URN non null
	 */
	private static URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//-----
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}
}
