package io.vertigo.dynamo.work.distributed.rest;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.xml.XMLModulesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Charge et d�marre un environnement.
 * @author pchretien, npiedeloup
 */
public final class Starter implements Runnable {
	private static boolean SILENCE = true;
	private final Class<?> relativeRootClass;
	private final String managersXmlFileName;
	private final Option<String> propertiesFileName;
	private final Option<Properties> defaultProperties;
	private final long timeToWait;
	private boolean started;

	/**
	 * @param managersXmlFileName Fichier managers.xml
	 * @param propertiesFileName Fichier de propri�t�s
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 * @param defaultProperties Propri�t�s par d�faut (pouvant �tre r�cup�r� de la ligne de commande par exemple)
	 * @param timeToWait Temps d'attente, 0 signifie illimit�
	 */
	public Starter(final String managersXmlFileName, final Option<String> propertiesFileName, final Class<?> relativeRootClass, final Option<Properties> defaultProperties, final long timeToWait) {
		Assertion.checkNotNull(managersXmlFileName);
		Assertion.checkNotNull(propertiesFileName);
		Assertion.checkNotNull(defaultProperties);
		//---------------------------------------------------------------------
		this.managersXmlFileName = managersXmlFileName;
		this.propertiesFileName = propertiesFileName;
		this.defaultProperties = defaultProperties;
		this.timeToWait = timeToWait;
		this.relativeRootClass = relativeRootClass;

	}

	/**
	 * Lance l'environnement et attend ind�finiment.
	 * @param args "Usage: java kasper.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final String usageMsg = "Usage: java kasper.kernel.Starter managers.xml <conf.properties>";
		Assertion.checkArgument(args.length >= 1 && args.length <= 2, usageMsg + " (" + args.length + ")");
		Assertion.checkArgument(args[0].endsWith(".xml"), usageMsg + " (" + args[0] + ")");
		Assertion.checkArgument(args.length == 1 || args[1].endsWith(".properties"), usageMsg + " (" + (args.length == 2 ? args[1] : "vide") + ")");
		//---------------------------------------------------------------------
		final String managersXmlFileName = args[0];
		final Option<String> propertiesFileName = args.length == 2 ? Option.<String> some(args[1]) : Option.<String> none();
		final Starter starter = new Starter(managersXmlFileName, propertiesFileName, Starter.class, Option.<Properties> none(), 0);
		starter.run();
	}

	/** {@inheritDoc} */
	public void run() {
		try {
			start();

			final Object lock = new Object();
			synchronized (lock) {
				lock.wait(timeToWait * 1000); //on attend le temps demand� et 0 => illimit�
			}
		} catch (final InterruptedException e) {
			//rien arret normal
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			stop();
		}
	}

	/**
	 * D�marre l'application.
	 */
	public final void start() {
		// Cr�ation de l'�tat de l'application
		// Initialisation de l'�tat de l'application
		final URL xmlURL = createURL(managersXmlFileName, relativeRootClass);
		final Properties properties = new Properties();
		if (defaultProperties.isDefined()) {
			properties.putAll(defaultProperties.get());
		}
		appendFileProperties(properties, propertiesFileName, relativeRootClass);
		final ComponentSpaceConfig componentSpaceConfig = new ComponentSpaceConfigBuilder() //
				.withSilence(SILENCE) //
				.withLoader(new XMLModulesLoader(xmlURL, properties)) //
				.build();
		Home.start(componentSpaceConfig);
		started = true;
	}

	/**
	 * Stop l'application.
	 */
	public final void stop() {
		if (started) {
			Home.stop();
			started = false;
		}
	}

	/**
	 * Charge le fichier properties.
	 * Par defaut vide, mais il peut-�tre surcharg�.
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 */
	private static final void appendFileProperties(final Properties properties, final Option<String> propertiesFileName, final Class<?> relativeRootClass) {
		//---------------------------------------------------------------------
		if (propertiesFileName.isDefined()) {
			final String fileName = translateFileName(propertiesFileName.get(), relativeRootClass);
			try {
				try (final InputStream in = createURL(fileName, relativeRootClass).openStream()) {
					properties.load(in);
				}
			} catch (final IOException e) {
				throw new IllegalArgumentException("Impossible de charger le fichier de configuration des tests : " + fileName, e);
			}
		}
	}

	/**
	 * Transforme le chemin vers un fichier local au test en une URL absolue.
	 * @param fileName Path du fichier : soit en absolu (commence par /), soit en relatif � la racine
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 * @return URL du fichier
	 */
	private static final URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//---------------------------------------------------------------------
		final String absoluteFileName = translateFileName(fileName, relativeRootClass);
		try {
			return new URL(absoluteFileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouv�, on recherche dans le classPath 
			final URL url = relativeRootClass.getResource(absoluteFileName);
			Assertion.checkNotNull(url, "Impossible de r�cup�rer le fichier [" + absoluteFileName + "]");
			return url;
		}
	}

	private static final String translateFileName(final String fileName, final Class<?> relativeRootClass) {
		Assertion.checkArgNotEmpty(fileName);
		//---------------------------------------------------------------------
		if (fileName.startsWith(".")) {
			//soit en relatif
			return "/" + getRelativePath(relativeRootClass) + "/" + fileName.replace("./", "");
		}

		//soit en absolu		
		if (fileName.startsWith("/")) {
			return fileName;
		}
		return "/" + fileName;
	}

	private static final String getRelativePath(final Class<?> relativeRootClass) {
		return relativeRootClass.getPackage().getName().replace('.', '/');
	}
}
