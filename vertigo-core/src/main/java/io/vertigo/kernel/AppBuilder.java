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
package io.vertigo.kernel;

import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.xml.XMLModulesLoader;

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
	private ComponentSpaceConfigBuilder componentSpaceConfigBuilder;
	private boolean silence;
	private final Properties envParams = new Properties();
	private final List<URL> xmlUrls = new ArrayList<>();

	/**
	 * Application state.
	 */
	public static class App implements AutoCloseable {
		private final Properties envParams;

		/**
		 * Constructor.
		 * @param envParams Environment Parameters (are public in app)
		 * @param componentSpaceConfig ComponentSpace Config
		 */
		App(final Properties envParams, final ComponentSpaceConfig componentSpaceConfig) {
			Assertion.checkNotNull(envParams, "envParams");
			//---------------------------------------------------------------------
			this.envParams = envParams;
			Home.start(componentSpaceConfig);
		}

		/** {@inheritDoc} */
		public void close() {
			Home.stop();
		}

		/**
		 * @return Environment Parameters
		 */
		public Properties getEnvParams() {
			return envParams;
		}
	}

	/**
	 * @param newSilence silence mode
	 * @return this builder
	 */
	public AppBuilder withSilence(final boolean newSilence) {
		silence = newSilence;
		return this;
	}

	/**
	 * @param newComponentSpaceConfigBuilder ComponentSpaceConfigBuilder to use (can be completed outside this builder)
	 * @return this builder
	 */
	public AppBuilder withComponentSpaceConfigBuilder(final ComponentSpaceConfigBuilder newComponentSpaceConfigBuilder) {
		Assertion.checkState(componentSpaceConfigBuilder == null, "componentSpaceConfigBuilder was already set");
		//---------------------------------------------------------------------
		componentSpaceConfigBuilder = newComponentSpaceConfigBuilder;
		return this;
	}

	/**
	 * Append EnvParams.
	 * @param newEnvParams envParams
	 * @return this builder
	 */
	public AppBuilder withEnvParams(final Properties newEnvParams) {
		envParams.putAll(newEnvParams);
		return this;
	}

	/**
	 * Append EnvParams.
	 * @param optionEnvParams Option of envParams
	 * @return this builder
	 */
	public AppBuilder withEnvParams(final Option<Properties> optionEnvParams) {
		if (optionEnvParams.isDefined()) {
			envParams.putAll(optionEnvParams.get());
		}
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
			envParams.putAll(loadProperties(optionEnvParams.get(), relativeRootClass));
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
			envParams.putAll(loadProperties(newEnvParam, relativeRootClass));
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
	public ComponentSpaceConfigBuilder flushToBuilder() {
		Assertion.checkState(componentSpaceConfigBuilder != null, "componentSpaceConfigBuilder was not set, use build instead");
		//---------------------------------------------------------------------
		componentSpaceConfigBuilder.withSilence(silence); //
		//1- if no xmlUrls we check if a property reference files
		if (xmlUrls.isEmpty()) {
			final String xmlFileNames = envParams.getProperty("applicationConfiguration");
			Assertion.checkNotNull(xmlFileNames, "'applicationConfiguration' property not found in EnvironmentParams");
			final String[] xmlFileNamesSplit = xmlFileNames.split(";");
			withXmlFileNames(Object.class, xmlFileNamesSplit);
		}
		//---------------------------------------------------------------------
		//2- We load XML with Loaders to componentSpaceConfigBuilder
		Assertion.checkArgument(!xmlUrls.isEmpty(), "We need at least one Xml file");
		final List<XMLModulesLoader> moduleLoaders = new ArrayList<>();
		for (final URL xmlUrl : xmlUrls) {
			moduleLoaders.add(new XMLModulesLoader(xmlUrl, envParams));
		}
		//.withRestEngine(new GrizzlyRestEngine(8086));
		for (final XMLModulesLoader modulesLoader : moduleLoaders) {
			componentSpaceConfigBuilder.withLoader(modulesLoader);
		}
		return componentSpaceConfigBuilder;
	}

	/**
	 * @return Build the ComponentSpaceConfig (can be use to start a Home)
	 */
	public ComponentSpaceConfig build() {
		if (componentSpaceConfigBuilder == null) {
			withComponentSpaceConfigBuilder(new ComponentSpaceConfigBuilder());
		}
		return flushToBuilder().build();
	}

	/**
	 * Build this App and start it.
	 * @return App as Application
	 */
	public App start() {
		//1- build
		//2- start
		//3- return autoCloseable to stop Home
		return new App(envParams, build());
	}

	private static Properties loadProperties(final String propertiesName, final Class<?> relativePathBase) {
		try {
			final Properties properties = new Properties();
			try (final InputStream in = AppBuilder.createURL(propertiesName, relativePathBase).openStream()) {
				properties.load(in);
			}
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
		//---------------------------------------------------------------------
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouvé, on recherche dans le classPath 
			final URL url = relativeRootClass.getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

	//	/**
	//	 * Transforme le chemin vers un fichier local au test en une URL absolue.
	//	 * @param fileName Path du fichier : soit en absolu (commence par /), soit en relatif à la racine
	//	 * @param relativeRootClass Racine du chemin relatif, le cas echéant
	//	 * @return URL du fichier
	//	 */
	//	private static final URL createURL(final String fileName, final Class<?> relativeRootClass) {
	//		Assertion.checkArgNotEmpty(fileName);
	//		//---------------------------------------------------------------------
	//		final String absoluteFileName = translateFileName(fileName, relativeRootClass);
	//		try {
	//			return new URL(absoluteFileName);
	//		} catch (final MalformedURLException e) {
	//			//Si fileName non trouvé, on recherche dans le classPath 
	//			final URL url = relativeRootClass.getResource(absoluteFileName);
	//			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + absoluteFileName + "]");
	//			return url;
	//		}
	//	}
	//
	//	private static final String translateFileName(final String fileName, final Class<?> relativeRootClass) {
	//		Assertion.checkArgNotEmpty(fileName);
	//		//---------------------------------------------------------------------
	//		if (fileName.startsWith(".")) {
	//			//soit en relatif
	//			return "/" + getRelativePath(relativeRootClass) + "/" + fileName.replace("./", "");
	//		}
	//
	//		//soit en absolu		
	//		if (fileName.startsWith("/")) {
	//			return fileName;
	//		}
	//		return "/" + fileName;
	//	}
	//
	//	private static final String getRelativePath(final Class<?> relativeRootClass) {
	//		return relativeRootClass.getPackage().getName().replace('.', '/');
	//	}

	//	/**
	//	 * Retourne une propriété non null.
	//	 * @param properties Propriétés
	//	 * @param propertyName Nom de la propriété recherchée
	//	 * @param messageIfNull Message en cas de propriété non trouvée
	//	 * @return Valeur de la propriété
	//	 */
	//	private static String getPropertyNotNull(final Properties properties, final String propertyName, final String messageIfNull) {
	//		Assertion.checkNotNull(properties);
	//		Assertion.checkNotNull(propertyName);
	//		//---------------------------------------------------------------------
	//		final String property = properties.getProperty(propertyName, null);
	//		Assertion.checkNotNull(property, messageIfNull);
	//		return property.trim();
	//	}

}
