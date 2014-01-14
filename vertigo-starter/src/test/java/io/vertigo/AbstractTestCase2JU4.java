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
package io.vertigo;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.Container;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.di.injector.Injector;

import org.junit.After;
import org.junit.Before;

/**
 * Charge l'environnement de test par defaut.
 * @author pchretien
 */
public abstract class AbstractTestCase2JU4 {
	protected final void nop(final Object o) {
		//rien
	}

	protected abstract void configMe(ComponentSpaceConfigBuilder componentSpaceConfiguilder);

	@Before
	public final void setUp() throws Exception {

		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder().withSilence(true);
		//final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder().withRestEngine(new GrizzlyRestEngine(8086)).withSilence(true);
		configMe(componentSpaceConfigBuilder);
		Home.start(componentSpaceConfigBuilder.build());

		//On injecte les managers sur la classe de test.
		final Injector injector = new Injector();
		injector.injectMembers(this, getContainer());

		doSetUp();
	}

	@After
	public final void tearDown() throws Exception {
		try {
			doTearDown();
		} finally {
			Home.stop();
		}
		doAfterTearDown();
	}

	/**
	 * Initialisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doSetUp() throws Exception {
		// pour implé spécifique 
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doTearDown() throws Exception {
		// pour implé spécifique 
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	protected void doAfterTearDown() throws Exception {
		// pour implé spécifique 
	}

	/**
	 * Fournit le container utilisé pour l'injection.
	 * @return Container de l'injection
	 */
	private Container getContainer() {
		return Home.getComponentSpace();
	}
}
