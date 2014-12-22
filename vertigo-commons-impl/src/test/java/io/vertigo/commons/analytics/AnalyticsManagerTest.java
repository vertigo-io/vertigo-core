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
package io.vertigo.commons.analytics;

import io.vertigo.AbstractTestCaseJU4;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Cas de Test JUNIT de l'API Analytics.
 *
 * @author pchretien, npiedeloup
 */
public final class AnalyticsManagerTest extends AbstractTestCaseJU4 {

	/** Base de données gérant les articles envoyés dans une commande. */
	private static final String PROCESS_TYPE = "ARTICLE";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private AnalyticsManager analyticsManager;

	/**
	 * Test simple avec deux compteurs.
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 */
	@Test
	public void test1000Articles() {
		analyticsManager.getAgent().startProcess(PROCESS_TYPE, "1000 Articles 25 Kg");
		for (int i = 0; i < 1000; i++) {
			analyticsManager.getAgent().incMeasure("POIDS", 25);
			analyticsManager.getAgent().incMeasure("MONTANT", 10);
		}
		analyticsManager.getAgent().stopProcess();
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		analyticsManager.getAgent().incMeasure("POIDS", 25);
		//Dans le cas du dummy ça doit passer
	}

	/**
	 * Même test après désactivation.
	 */
	@Test
	public void testOff() {
		test1000Articles();
	}

	/**
	 * Test de récursivité.
	 * Test sur l'envoi de 1000 commandes contenant chacune 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 * Les frais d'envoi sont de 5€.
	 */
	@Test
	public void test1000Commandes() {
		final long start = System.currentTimeMillis();
		analyticsManager.getAgent().startProcess(PROCESS_TYPE, "1000 Commandes");
		for (int i = 0; i < 1000; i++) {
			analyticsManager.getAgent().incMeasure("MONTANT", 5);
			test1000Articles();
		}
		analyticsManager.getAgent().stopProcess();
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
	}
}
