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

import io.vertigo.app.App;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.xml.XMLAppConfigBuilder;
import io.vertigo.lang.Assertion;

import java.util.Properties;

/**
 * Charge et démarre un environnement.
 * @author pchretien, npiedeloup
 */
public final class Starter implements Runnable {
	private final AppConfig appConfig;
	private final long timeToWait;

	/**
	 * @param managersXmlFileName Fichier managers.xml
	 * @param relativeRootClass Racine du chemin relatif, le cas échéant
	 * @param timeToWait Temps d'attente en ms, 0 signifie illimité
	 */
	public Starter(final String managersXmlFileName, final Class<?> relativeRootClass, final long timeToWait) {
		Assertion.checkNotNull(managersXmlFileName);
		//-----
		this.timeToWait = timeToWait;
		// Initialisation de l'état de l'application
		appConfig = new XMLAppConfigBuilder()
				.withModules(relativeRootClass, new Properties(), managersXmlFileName)
				.build();
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try (App app = new App(appConfig)) {
			System.out.println("Node started (timout in " + timeToWait / 1000 + "s)");
			if (timeToWait > 0) {
				Thread.sleep(timeToWait);
			} else {
				//infinite
				while (!Thread.currentThread().isInterrupted()) {
					Thread.sleep(60 * 1000);
				}
			}
			System.out.println("Node stopping by timeout");
		} catch (final InterruptedException e) {
			//rien arret normal
			System.out.println("Node stopping by interrupted");
		} catch (final Exception e) {
			System.err.println("Application error, exit " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
}
