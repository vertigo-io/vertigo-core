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
package vertigo.trash.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import vertigo.kernel.Home;
import vertigo.kernel.di.configurator.HomeConfig;
import vertigo.kernel.lang.Assertion;
import vertigo.kernel.lang.Option;
import vertigo.kernel.util.PropertiesUtil;


/**
 * Charge et d�marre un environnement.
 * @author pchretien
 */
public final class Starter implements Runnable {
	private static boolean SILENCE = true;
	private final Class<?> relativeRootClass;
	private final String managersXmlFileName;
	private final Option<String> propertiesFileName;
	private final long timeToWait;
	private boolean started;

	/**
	 * @param managersXmlFileName Fichier managers.xml
	 * @param propertiesFileName Fichier de propri�t�s
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 * @param timeToWait Temps d'attente, 0 signifie illimit�
	 */
	public Starter(final String managersXmlFileName, final Option<String> propertiesFileName, final Class<?> relativeRootClass, final long timeToWait) {
		Assertion.notNull(managersXmlFileName);
		Assertion.notNull(propertiesFileName);
		//---------------------------------------------------------------------
		this.managersXmlFileName = managersXmlFileName;
		this.propertiesFileName = propertiesFileName;
		this.timeToWait = timeToWait;
		this.relativeRootClass = relativeRootClass;
	}

	/**
	 * Lance l'environnement et attend ind�finiment.
	 * @param args "Usage: java vertigo.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final String usageMsg = "Usage: java vertigo.kernel.Starter managers.xml <conf.properties>";
		Assertion.precondition(args.length >= 1 && args.length <= 2, usageMsg + " (" + args.length + ")");
		Assertion.precondition(args[0].endsWith(".xml"), usageMsg + " (" + args[0] + ")");
		Assertion.precondition(args.length == 1 || args[1].endsWith(".properties"), usageMsg + " (" + (args.length == 2 ? args[1] : "vide") + ")");
		//---------------------------------------------------------------------
		final String managersXmlFileName = args[0];
		final Option<String> propertiesFileName = args.length == 2 ? Option.<String> some(args[1]) : Option.<String> none();
		final Starter starter = new Starter(managersXmlFileName, propertiesFileName, Starter.class, 0);
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
		final Properties properties = loadProperties(propertiesFileName, relativeRootClass);
		final HomeConfig homeConfig = new XMLHomeConfigBuilder(xmlURL, properties, SILENCE).build();
		Home.start(homeConfig);
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
	 * @return properties utilis�s pour d�marrer les managers.
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 */
	private static final Properties loadProperties(final Option<String> propertiesFileName, final Class<?> relativeRootClass) {
		final Properties properties = new Properties();
		//---------------------------------------------------------------------
		if (propertiesFileName.isDefined()) {
			final String fileName = translateFileName(propertiesFileName.get(), relativeRootClass);
			try {
				final InputStream in = PropertiesUtil.createURL(fileName).openStream();
				try {
					properties.load(in);
				} finally {
					in.close();
				}
			} catch (final IOException e) {
				throw new IllegalArgumentException("Impossible de charger le fichier de configuration des tests : " + fileName, e);
			}
		}
		return properties;
	}

	/**
	 * Transforme le chemin vers un fichier local au test en une URL absolue.
	 * @param fileName Path du fichier : soit en absolu (commence par /), soit en relatif � la racine
	 * @param relativeRootClass Racine du chemin relatif, le cas ech�ant
	 * @return URL du fichier
	 */
	private static final URL createURL(final String fileName, final Class<?> relativeRootClass) {
		Assertion.notEmpty(fileName);
		//---------------------------------------------------------------------
		final String absoluteFileName = translateFileName(fileName, relativeRootClass);
		try {
			return new URL(absoluteFileName);
		} catch (final MalformedURLException e) {
			//Si fileName non trouv�, on recherche dans le classPath 
			final URL url = relativeRootClass.getResource(absoluteFileName);
			Assertion.notNull(url, "Impossible de r�cup�rer le fichier [" + absoluteFileName + "]");
			return url;
		}
	}

	private static final String translateFileName(final String fileName, final Class<?> relativeRootClass) {
		Assertion.notEmpty(fileName);
		//---------------------------------------------------------------------
		if (fileName.startsWith(".")) {
			//soit en relatif
			return "/" + getRelativePath(relativeRootClass) + "/" + fileName;
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
