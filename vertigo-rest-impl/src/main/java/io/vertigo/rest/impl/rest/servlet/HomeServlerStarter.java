package io.vertigo.rest.impl.rest.servlet;

import io.vertigo.kernel.AppBuilder;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.rest.plugins.rest.servlet.ServletResourceResolverPlugin;
import io.vertigo.rest.plugins.rest.servlet.WebAppContextConfigPlugin;

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
final class HomeServlerStarter {
	private static final Logger LOG = Logger.getLogger(HomeServlerStarter.class);

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

			final ComponentSpaceConfig componentSpaceConfig = new AppBuilder() //
					.withSilence(true)//
					.withEnvParams(conf).build();
			// Initialisation de l'état de l'application
			Home.start(componentSpaceConfig);

			servletListener.onServletStart(getClass().getName());
		} catch (final Throwable t) {
			LOG.error(t.getMessage(), t);
			t.printStackTrace();
			throw new RuntimeException("Problème d'initialisation de l'application", t);
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
			e.printStackTrace();
		}
	}
}
