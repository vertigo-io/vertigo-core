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
package io.vertigo.struts2.impl.servlet;

import io.vertigo.app.App;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.xml.XMLAppConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.struts2.plugins.config.servlet.WebAppContextParamPlugin;
import io.vertigo.struts2.plugins.resource.servlet.ServletResourceResolverPlugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

/**
 * @author npiedeloup
 */
final class HomeServletStarter {
	private static final Logger LOG = Logger.getLogger(HomeServletStarter.class);

	private static final String BOOT_PROPERTIES_PREFIX = "boot.";
	private static final String EXTERNAL_PROPERTIES_PARAM_NAME = "external-properties";
	private static final String LOG4J_CONFIGURATION_PARAM_NAME = "log4j.configurationFileName";

	/** Servlet listener */
	private final ServletListener servletListener = new ServletListener();

	private App app;

	/**
	 * Initialize application.
	 * @param servletContext ServletContext
	 */
	public void contextInitialized(final ServletContext servletContext) {
		final long start = System.currentTimeMillis();
		try {
			// Initialisation du web context de l'application (porteur des singletons applicatifs)
			ServletResourceResolverPlugin.setServletContext(servletContext);
			// Création de l'état de l'application
			// Lecture des paramètres de configuration
			final Map<String, String> webParams = createWebParams(servletContext);
			WebAppContextParamPlugin.setParams(webParams);
			//-----
			final Properties bootConf = createBootProperties(servletContext);
			Assertion.checkArgument(bootConf.containsKey("boot.applicationConfiguration"), "Param \"boot.applicationConfiguration\" is mandatory, check your .properties or web.xml.");

			final XMLAppConfigBuilder appConfigBuilder = new XMLAppConfigBuilder();
			appConfigBuilder.beginBoot()
					.silently()
					.endBoot();

			//si présent on récupère le paramétrage du fichier externe de paramétrage log4j
			if (bootConf.containsKey(LOG4J_CONFIGURATION_PARAM_NAME)) {
				final String logFileName = bootConf.getProperty(LOG4J_CONFIGURATION_PARAM_NAME);
				bootConf.remove(LOG4J_CONFIGURATION_PARAM_NAME);
				//-----
				appConfigBuilder.withLogConfig(new LogConfig(logFileName));
			}

			final String xmlModulesFileNames = bootConf.getProperty("boot.applicationConfiguration");
			final String[] xmlFileNamesSplit = xmlModulesFileNames.split(";");
			bootConf.remove("boot.applicationConfiguration");
			//-----
			appConfigBuilder.withModules(getClass(), bootConf, xmlFileNamesSplit);

			// Initialisation de l'état de l'application
			app = new App(appConfigBuilder.build());

			servletListener.onServletStart(getClass().getName());
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw new WrappedException("Problème d'initialisation de l'application", e);
		} finally {
			if (LOG.isInfoEnabled()) {
				LOG.info("Temps d'initialisation du listener " + (System.currentTimeMillis() - start));
			}
		}
	}

	/**
	 * Création des propriétés à partir du Web XML : utilisé par le plugin WebAppConfigPlugin du ParamManager.
	 * @return Properties
	 */
	private static Map<String, String> createWebParams(final ServletContext servletContext) {
		// ======================================================================
		// ===Conversion en Properties du fichier de paramétrage de la servlet===
		// ======================================================================
		final Map<String, String> servletParams = new HashMap<>();
		String name;
		/*
		 * On récupère les paramètres du context (web.xml ou fichier tomcat par exemple) Ces paramètres peuvent
		 * surcharger les paramètres de la servlet de façon à créer un paramétrage adhoc de développement par exemple.
		 */
		for (final Enumeration<String> enumeration = servletContext.getInitParameterNames(); enumeration.hasMoreElements();) {
			name = enumeration.nextElement();
			servletParams.put(name, servletContext.getInitParameter(name));
		}
		return servletParams;
	}

	/**
	 * Création des propriétés à partir des différents fichiers de configuration. - Web XML - Fichier externe défini par
	 * la valeur de la propriété système : external-properties
	 *
	 * @return Properties
	 */
	private static Properties createBootProperties(final ServletContext servletContext) {
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
			if (name.startsWith(BOOT_PROPERTIES_PREFIX)) {
				servletParams.put(name, servletContext.getInitParameter(name));
			}
		}
		if (servletParams.isEmpty()) {
			LOG.warn("None parameters had been loaded from servletcontext. Check they all have the prefix : " + BOOT_PROPERTIES_PREFIX);
		}

		/*
		 * On récupère le paramètre du fichier de configuration des logs externe (-Dlog4j.configurationFileName).
		 * Ce paramètre peut pointer sur un fichier de la webapp ou du FS.
		 * Il peut aussi être dans le web.xml ou le EXTERNAL_PROPERTIES_PARAM_NAME
		 */
		String log4jConfigurationFileName = System.getProperty(LOG4J_CONFIGURATION_PARAM_NAME);
		if (log4jConfigurationFileName == null) {
			log4jConfigurationFileName = servletContext.getInitParameter(LOG4J_CONFIGURATION_PARAM_NAME);
		}
		if (log4jConfigurationFileName != null) {
			servletParams.put(LOG4J_CONFIGURATION_PARAM_NAME, log4jConfigurationFileName);
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
			throw new WrappedException("Erreur lors de la lecture du fichier", e);
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
	 * Called when this servlet is stopped.
	 * @param servletContext Servlet Context
	 */
	public void contextDestroyed(final ServletContext servletContext) {
		app.close();
		servletListener.onServletDestroy(getClass().getName());
	}
}
