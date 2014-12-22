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

import io.vertigo.core.Home;
import io.vertigo.core.config.AppBuilder;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.lang.Component;
import io.vertigo.lang.Container;
import io.vertigo.lang.Describable;
import io.vertigo.lang.Option;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Classe parente de tous les TNR associés à vertigo.
 *
 * @author jmforhan
 */
public abstract class AbstractTestCaseJU4 {
	private static boolean homeStarted;

	private synchronized void startHome() {
		final AppConfigBuilder appConfigBuilder = new AppConfigBuilder();
		configMe(appConfigBuilder);
		Home.start(appConfigBuilder.build());
		homeStarted = true;
	}

	private synchronized void stopHome() {
		Home.stop();
		homeStarted = false;
	}

	/**
	 * Récupère la valeur de homeStarted.
	 *
	 * @return valeur de homeStarted
	 */
	private static synchronized boolean isHomeStarted() {
		return homeStarted;
	}

	/**
	 * Doit-on s'assurer que le Home est réinitialisé avant le début de chaque test?
	 * Par défaut, return true.
	 *
	 * @return booléen
	 */
	protected boolean cleanHomeForTest() {
		return true;
	}

	/**
	 * Méthode ne faisant rien.
	 *
	 * @param o object
	 */
	protected final void nop(final Object o) {
		// rien
	}

	/**
	 * Set up de l'environnement de test.
	 *
	 * @throws Exception exception
	 */
	@Before
	public final void setUp() throws Exception {
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		if (cleanHomeForTest() && isHomeStarted()) {
			stopHome();
		}
		if (!isHomeStarted()) {
			startHome();
		}
		// On injecte les managers sur la classe de test.
		Injector.injectMembers(this, getContainer());
		doSetUp();
	}

	/**
	 * Tear down de l'environnement de test.
	 *
	 * @throws Exception Exception
	 */
	@After
	public final void tearDown() throws Exception {
		try {
			doTearDown();
		} finally {
			if (cleanHomeForTest()) {
				stopHome();
			}
		}
		doAfterTearDown();
	}

	/**
	 * Initialisation du test pour implé spécifique.
	 *
	 * @throws Exception Erreur
	 */
	protected void doSetUp() throws Exception {
		// pour implé spécifique
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 *
	 * @throws Exception Erreur
	 */
	protected void doTearDown() throws Exception {
		// pour implé spécifique
	}

	/**
	 * Finalisation du test pour implé spécifique après le tear down.
	 *
	 * @throws Exception Erreur
	 */
	protected void doAfterTearDown() throws Exception {
		// pour implé spécifique
	}

	/**
	 * Fournit le container utilisé pour l'injection.
	 *
	 * @return Container de l'injection
	 */
	private static Container getContainer() {
		return Home.getComponentSpace();
	}

	/**
	 * Tableau des fichiers managers.xml a prendre en compte.
	 *
	 * @return fichier managers.xml (par defaut managers-test.xml)
	 */
	protected String[] getManagersXmlFileName() {
		return new String[] { "./managers-test.xml", };
	}

	/**
	 * Fichier de propriétés de paramétrage des managers.
	 *
	 * @return fichier properties de paramétrage des managers (par defaut Option.none())
	 */
	protected Option<String> getPropertiesFileName() {
		return Option.none(); // par défaut pas de properties
	}

	/**
	* Utilitaire.
	* @param manager Manager
	*/
	protected static final void testDescription(final Component manager) {
		if (manager instanceof Describable) {
			final List<ComponentInfo> componentInfos = Describable.class.cast(manager).getInfos();
			for (final ComponentInfo componentInfo : componentInfos) {
				Assert.assertNotNull(componentInfo);
			}
		}
	}

	/**
	 * Configuration des tests.
	 */
	protected void configMe(final AppConfigBuilder appConfigBuilder) {
		final AppBuilder appBuilder = new AppBuilder()
				.withSilence(true)
				.withAppConfigBuilder(appConfigBuilder)
				.withXmlFileNames(getClass(), getManagersXmlFileName())
				.withEnvParams(getClass(), getPropertiesFileName());
		appBuilder.toAppConfigBuilder();
	}
}
