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
package io.vertigo.dynamo.work.distributed.rest;

import io.vertigo.core.AppBuilder;
import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;

import java.util.Properties;

/**
 * Charge et démarre un environnement.
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
	 * @param propertiesFileName Fichier de propriétés
	 * @param relativeRootClass Racine du chemin relatif, le cas échéant
	 * @param defaultProperties Propriétés par défaut (pouvant être récupéré de la ligne de commande par exemple)
	 * @param timeToWait Temps d'attente, 0 signifie illimité
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
	 * Lance l'environnement et attend indéfiniment.
	 * @param args "Usage: java kasper.kernel.Starter managers.xml <conf.properties>"
	 */
	public static void main(final String[] args) {
		final String usageMsg = "Usage: java " + Starter.class.getName() + " managers.xml <conf.properties>";
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
				lock.wait(timeToWait * 1000); //on attend le temps demandé et 0 => illimité
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
	 * Démarre l'application.
	 */
	public final void start() {
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		//final URL xmlURL = createURL(managersXmlFileName, relativeRootClass);
		final AppBuilder builder = new AppBuilder() //
				.withSilence(SILENCE) //
				.withXmlFileNames(relativeRootClass, managersXmlFileName) //
				.withEnvParams(defaultProperties) //
				.withEnvParams(relativeRootClass, propertiesFileName);
		Home.start(builder.build());
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
}
