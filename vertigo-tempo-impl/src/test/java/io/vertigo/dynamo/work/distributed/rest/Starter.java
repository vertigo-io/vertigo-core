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

import io.vertigo.boot.xml.XMLAppConfigBuilder;
import io.vertigo.core.Home.App;
import io.vertigo.core.config.AppConfig;
import io.vertigo.lang.Assertion;

import org.apache.log4j.Logger;

/**
 * Charge et démarre un environnement.
 * @author pchretien, npiedeloup
 */
public final class Starter implements Runnable {
	private final Logger log = Logger.getLogger(getClass());
	private final AppConfig appConfig;
	private final long timeToWait;

	/**
	 * @param managersXmlFileName Fichier managers.xml
	 * @param relativeRootClass Racine du chemin relatif, le cas échéant
	 * @param timeToWait Temps d'attente, 0 signifie illimité
	 */
	public Starter(final String managersXmlFileName, final Class<?> relativeRootClass, final long timeToWait) {
		Assertion.checkNotNull(managersXmlFileName);
		//-----
		this.timeToWait = timeToWait;
		appConfig = new XMLAppConfigBuilder()
				.withSilence(true)
				.withXmlFileNames(relativeRootClass, managersXmlFileName)
				.build();

	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		try (App app = new App(appConfig)) {
			final Object lock = new Object();
			synchronized (lock) {
				lock.wait(timeToWait * 1000); //on attend le temps demandé et 0 => illimité
			}
		} catch (final InterruptedException e) {
			//rien arret normal
		} catch (final Exception e) {
			log.error("Application error, exit", e);
		}
	}
}
