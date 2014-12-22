/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.config;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.xml.XMLModulesParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Build a App from conventional elements.
 * App are start at create time and are autocloseable.
 *
 * @author npiedeloup
 */
public final class AppBuilder {
	private AppConfigBuilder myAppConfigBuilder;
	private boolean mySilence;
	private final Properties myEnvParams = new Properties();
	private final List<URL> xmlUrls = new ArrayList<>();

	/**
	 * @param newSilence silence mode
	 * @return this builder
	 */
	public AppBuilder withSilence(final boolean newSilence) {
		mySilence = newSilence;
		return this;
	}

	/**
	 * @param appConfigBuilder appConfigBuilder to use (can be completed outside this builder)
	 * @return this builder
	 */
	public AppBuilder withAppConfigBuilder(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkState(myAppConfigBuilder == null, "componentSpaceConfigBuilder was already set");
		//-----
		this.myAppConfigBuilder = appConfigBuilder;
		return this;
	}

	/**
	 * Append EnvParams.
	 * @param envParams envParams
	 * @return this builder
	 */
	public AppBuilder withEnvParams(final Properties envParams) {
		Assertion.checkNotNull(envParams);
		//-----
		myEnvParams.putAll(envParams);
		return this;
	}

	/**
	 * Append EnvParams.
	 * @param relativeRootClass Class use for relative path
	 * @param optionEnvParams Option of envParamsFileName
	 * @return this builder
	 */
	public AppBuilder withEnvParams(final Class<?> relativeRootClass, final Option<String> optionEnvParams) {
		if (optionEnvParams.isDefined()) {
			withEnvParams(loadProperties(optionEnvParams.get(), relativeRootClass));
		}
		return this;
	}

	/**
	 * Append EnvParams.
	 * @param relativeRootClass Class use for relative path
	 * @param newEnvParams Multiple EnvParamsFileName
	 * @return this builder
	 */
	public AppBuilder withEnvParams(final Class<?> relativeRootClass, final String... newEnvParams) {
		for (final String newEnvParam : newEnvParams) {
			withEnvParams(loadProperties(newEnvParam, relativeRootClass));
		}
		return this;
	}

	/**
	 * Append XmlFiles.
	 * @param relativeRootClass Class use for relative path
	 * @param xmlFileNames Multiple xmlFileName
	 * @return this builder
	 */
	public AppBuilder withXmlFileNames(final Class<?> relativeRootClass, final String... xmlFileNames) {
		for (final String xmlFileName : xmlFileNames) {
			final URL xmlUrl = createURL(xmlFileName, relativeRootClass);
			xmlUrls.add(xmlUrl);
		}
		return this;
	}

	/**
	 * Update the 'already set' componentSpaceConfigBuilder and return it.
	 * @return ComponentSpaceConfigBuilder
	 */
	public AppConfigBuilder toAppConfigBuilder() {
		Assertion.checkState(myAppConfigBuilder != null, "appConfigBuilder was not set, use build instead");
		//-----
		myAppConfigBuilder.withSilence(mySilence);
		//1- if no xmlUrls we check if a property reference files
		if (xmlUrls.isEmpty()) {
			final String xmlFileNames = myEnvParams.getProperty("applicationConfiguration");
			Assertion.checkNotNull(xmlFileNames, "'applicationConfiguration' property not found in EnvironmentParams");
			final String[] xmlFileNamesSplit = xmlFileNames.split(";");
			withXmlFileNames(getClass(), xmlFileNamesSplit);
		}
		//-----
		//2- We load XML with parser to obtain all the moduleConfigs
		Assertion.checkArgument(!xmlUrls.isEmpty(), "We need at least one Xml file");
		final XMLModulesParser parser = new XMLModulesParser(myEnvParams);
		for (final URL xmlUrl : xmlUrls) {
			myAppConfigBuilder.withModules(parser.parse(xmlUrl));
		}
		return myAppConfigBuilder;
	}

	/**
	 * @return Build the ComponentSpaceConfig (can be use to start a Home)
	 */
	public AppConfig build() {
		if (myAppConfigBuilder == null) {
			withAppConfigBuilder(new AppConfigBuilder());
		}
		return toAppConfigBuilder().build();
	}

	private static Properties loadProperties(final String propertiesName, final Class<?> relativePathBase) {
		try (final InputStream in = AppBuilder.createURL(propertiesName, relativePathBase).openStream()) {
			final Properties properties = new Properties();
			properties.load(in);
			return properties;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
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
