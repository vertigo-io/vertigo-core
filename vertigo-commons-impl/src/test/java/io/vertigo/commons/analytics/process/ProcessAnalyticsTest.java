/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.analytics.process;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.process.data.TestAProcessConnectorPlugin;
import io.vertigo.commons.analytics.process.data.TestAnalyticsAspectServices;

/**
 * Cas de Test JUNIT de l'API Analytics.
 *
 * @author pchretien, npiedeloup
 */
public final class ProcessAnalyticsTest extends AbstractTestCaseJU4 {
	private static final String PRICE = "PRICE";

	private static final String WEIGHT = "WEIGHT";

	/** Base de données gérant les articles envoyés dans une commande. */
	private static final String TEST_CATEGORY = "test";

	/** Logger. */
	private final Logger log = LogManager.getLogger(getClass());

	@Inject
	private AnalyticsManager analyticsManager;

	@Inject
	private TestAnalyticsAspectServices analyticsAspectServices;

	/**
	 * Test simple avec deux compteurs.
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg.
	 * Chaque article coute 10€.
	 */
	@Test
	public void test1000Articles() {
		analyticsManager.trace(
				TEST_CATEGORY,
				"/create/articles",
				tracer -> {
					for (int i = 0; i < 1000; i++) {
						tracer.incMeasure(WEIGHT, 25)
								.incMeasure(PRICE, 10);
					}
				});
	}

	/**
	 * Test pour vérifier que l'on peut se passer des processus si et seulement si le mode Analytics est désactivé.
	 */
	@Test
	public void testNoProcess() {
		analyticsManager.getCurrentTracer().ifPresent(
				tracer -> tracer.incMeasure(WEIGHT, 25));
		//Dans le cas du dummy ça doit passer
	}

	@Test
	public void testAspect() {
		TestAProcessConnectorPlugin.reset();
		final int result = analyticsAspectServices.add(1, 2);
		Assert.assertEquals(3, result);
		//---
		Assert.assertEquals(1, TestAProcessConnectorPlugin.getCount());
		Assert.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());
	}

	@Test
	public void testConnectors() {
		TestAProcessConnectorPlugin.reset();
		for (int i = 0; i < 50; i++) {
			final int result = analyticsAspectServices.add(i, 2 * i);
			Assert.assertEquals(3 * i, result);
		}
		for (int i = 0; i < 50; i++) {
			analyticsAspectServices.checkPositive(i);
		}
		Assert.assertEquals(100, TestAProcessConnectorPlugin.getCount());
		Assert.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());
	}

	@Test(expected = IllegalStateException.class)
	public void testFail() {
		TestAProcessConnectorPlugin.reset();
		try {
			analyticsAspectServices.checkPositive(-1);
		} catch (final IllegalStateException e) {
			Assert.assertEquals(1, TestAProcessConnectorPlugin.getCount());
			Assert.assertEquals("test", TestAProcessConnectorPlugin.getLastcategory());
			throw e;
		}

	}

	@Test
	public void testSetMeasures() {
		TestAProcessConnectorPlugin.reset();
		Assert.assertEquals(null, TestAProcessConnectorPlugin.getLastPrice());
		analyticsAspectServices.setMeasure();
		Assert.assertEquals(100D, TestAProcessConnectorPlugin.getLastPrice(), 0);
	}

	@Test
	public void testSetAndIncMeasures() {
		TestAProcessConnectorPlugin.reset();
		analyticsAspectServices.setAndIncMeasure();
		Assert.assertEquals(120D, TestAProcessConnectorPlugin.getLastPrice(), 0);
	}

	@Test
	public void testIncMeasures() {
		TestAProcessConnectorPlugin.reset();
		analyticsAspectServices.incMeasure();
		Assert.assertEquals(10D, TestAProcessConnectorPlugin.getLastPrice(), 0);
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
		analyticsManager.trace(
				TEST_CATEGORY,
				"/create/orders",
				tracer -> {
					for (int i = 0; i < 1000; i++) {
						tracer.incMeasure(PRICE, 5);
						test1000Articles();
					}
				});
		log.trace("elapsed = " + (System.currentTimeMillis() - start));
	}
}
