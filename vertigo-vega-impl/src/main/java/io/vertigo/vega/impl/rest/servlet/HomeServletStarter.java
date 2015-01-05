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
package io.vertigo.vega.impl.rest.servlet;

import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.vega.plugins.rest.servlet.ServletResourceResolverPlugin;
import io.vertigo.vega.plugins.rest.servlet.WebAppContextConfigPlugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

/**
 * @author npiedeloup
 */
final class HomeServletStarter {
	private static final Logger LOG = Logger.getLogger(HomeServletStarter.class);

	private static final String EXTERNAL_PROPERTIES_PARAM_NAME = "external-properties";

	/** clés dans le fichier Web.xml */

	/** Servlet listener */
	private final ServletListener servletListener = new ServletListener();

	/**
	 * Initialize and start Vertigo Home.
	 * @param servletContext ServletContext
	 */
	public final void contextInitialized(final ServletContext servletContext) {
		final long start = System.currentTimeMillis();
		try {
			// Initialisation du web context de l'application (porteur des singletons applicatifs)
			ServletResourceResolverPlugin.setServletContext(servletContext);
			// Création de l'état de l'application
			// Lecture des paramètres de configuration
			final Properties conf = createProperties(servletContext);
			WebAppContextConfigPlugin.setInitConfig(conf);

			final AppConfig appConfig = new AppConfigBuilder()
					.withSilence(true)
					.withEnvParams(conf)
					.build();
			// Initialisation de l'état de l'application
			Home.start(appConfig);
			servletListener.onServletStart(getClass().getName());
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException("Problème d'initialisation de l'application", e);
		} finally {
			if (LOG.isInfoEnabled()) {
				LOG.info("Temps d'initialisation du listener " + (System.currentTimeMillis() - start));
			}
		}
	}

	/**
	 * Création des propriétés à partir des différents fichiers de configuration. - Web XML - Fichier externe défini par
	 * la valeur de la propriété système : external-properties
	 *
	 * @return Properties
	 */
	private static Properties createProperties(final ServletContext servletContext) {
		// ======================================================================
		// ===Conversion en Properties du fichier de paramétrage de la servlet===
		// ======================================================================
		final Properties servletParams = new Properties();
		String name;

		/*
		 * On récupère les paramètres du context (web.xml ou fichier tomcat par exemple) Ces paramètres peuvent
		 * surcharger les paramètres de la servlet de façon à créer un paramétrage adhoc de développement par exemple.
		 */
		for (final Enumeration<String> enumeration = servletContext.getInitParameterNames(); enumeration.hasMoreElements();) {
			name = enumeration.nextElement();
			servletParams.put(name, servletContext.getInitParameter(name));
		}

		/*
		 * On récupère les paramètres du fichier de configuration externe (-Dexternal-properties). Ces paramètres
		 * peuvent surcharger les paramètres de la servlet de façon à créer un paramétrage adhoc de développement par
		 * exemple.
		 */
		final String externalPropertiesFileName = System.getProperty(EXTERNAL_PROPERTIES_PARAM_NAME);
		try {
			readFile(servletParams, externalPropertiesFileName);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur lors de la lecture du fichier", e);
		}

		return servletParams;
	}

	private static void readFile(final Properties servletParams, final String externalPropertiesFileName) throws IOException {
		if (externalPropertiesFileName != null) {
			try (final InputStream inputStream = new FileInputStream(externalPropertiesFileName)) {
				servletParams.load(inputStream);
			}
		}
	}

	/**
	 * Stop Vertigo Home.
	 * @param servletContext ServletContext
	 */
	public final void contextDestroyed(final ServletContext servletContext) {
		try {
			Home.stop();
			servletListener.onServletDestroy(getClass().getName());
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
