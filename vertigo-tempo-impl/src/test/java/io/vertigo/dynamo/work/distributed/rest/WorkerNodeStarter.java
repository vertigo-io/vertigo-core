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

import org.apache.log4j.Logger;

/**
 * @author npiedeloup
 */
public class WorkerNodeStarter {
	private static final Logger LOG = Logger.getLogger(WorkerNodeStarter.class);

	/**
	 * Lance l'environnement et attend indéfiniment.
	 * @param args "Usage: java vertigo.kernel.Starter managers.xml <conf.properties>"
	 * @throws InterruptedException Interrupt
	 */
	public static void main(final String[] args) throws InterruptedException {
		Assertion.checkArgument(args.length >= 1 && args.length <= 2, "Usage WorkerNodeStarter managers.xml <maxLifeTime>");
		//-----
		final long timeToWait = args.length == 2 ? Long.parseLong(args[1]) * 1000L : 5 * 60 * 1000L;

		final String managersXmlFileName = args[0];
		final AppConfig appConfig = new XMLAppConfigBuilder()
				.withModules(WorkerNodeStarter.class, new Properties(), managersXmlFileName)
				.build();

		LOG.info("Node starting");
		run(appConfig, timeToWait);
		LOG.info("Node stop");
	}

	private static void run(final AppConfig appConfig, final long timeToWait) {
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
