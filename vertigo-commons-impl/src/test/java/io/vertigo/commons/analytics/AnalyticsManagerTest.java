/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;

/**
 * Cas de Test JUNIT de l'API Analytics.
 *
 * @author pchretien, npiedeloup
 */
public final class AnalyticsManagerTest extends AbstractTestCaseJU4 {

	private static final String PRICE = "MONTANT";

	private static final String WEIGHT = "POIDS";

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
		analyticsManager.track(PROCESS_TYPE, "1000 Articles 25 Kg",
				tracker -> {
					for (int i = 0; i < 1000; i++) {
						tracker.incMeasure(WEIGHT, 25)
								.incMeasure(PRICE, 10);
					}
				});
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		analyticsManager.getCurrentTracker().ifPresent(
				tracker -> tracker.incMeasure(WEIGHT, 25));
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
		analyticsManager.track(PROCESS_TYPE, "1000 Commandes",
				tracker -> {
					for (int i = 0; i < 1000; i++) {
						tracker.incMeasure(PRICE, 5);
						test1000Articles();
					}
				});
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
	}
}
