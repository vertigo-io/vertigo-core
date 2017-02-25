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
package io.vertigo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.vertigo.app.App;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.xml.XMLAppConfigBuilder;
import io.vertigo.core.component.ComponentInfo;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.lang.Component;
import io.vertigo.lang.Describable;

/**
 * Classe parente de tous les TNR associés à vertigo.
 *
 * @author jmforhan
 */
public abstract class AbstractTestCaseJU4 {
	private AutoCloseableApp app;

	/**
	 * Set up de l'environnement de test.
	 *
	 * @throws Exception exception
	 */
	@BeforeEach
	@Before
	public final void setUp() throws Exception {
		app = new AutoCloseableApp(buildAppConfig());
		// On injecte les comosants sur la classe de test.
		DIInjector.injectMembers(this, app.getComponentSpace());
		doSetUp();
	}

	protected final App getApp() {
		return app;
	}

	/**
	 * Tear down de l'environnement de test.
	 *
	 * @throws Exception Exception
	 */
	@AfterEach
	@After
	public final void tearDown() throws Exception {
		try {
			doTearDown();
		} finally {
			if (app != null) {
				app.close();
			}
		}
		doAfterTearDown();
	}

	/**
	 * Méthode ne faisant rien.
	 *
	 * @param o object
	 */
	protected static final void nop(final Object o) {
		// rien
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
	 * Tableau des fichiers managers.xml a prendre en compte.
	 *
	 * @return fichier managers.xml (par defaut managers-test.xml)
	 */
	protected String[] getManagersXmlFileName() {
		return new String[] { "./managers-test.xml", };
	}

	/**
	* Utilitaire.
	* @param manager Manager
	*/
	protected static final void testDescription(final Component manager) {
		if (manager instanceof Describable) {
			final List<ComponentInfo> componentInfos = Describable.class.cast(manager).getInfos();
			for (final ComponentInfo componentInfo : componentInfos) {
				assertNotNull(componentInfo);
			}
		}
	}

	/**
	 * Configuration des tests.
	 * @return App config
	 */
	protected AppConfig buildAppConfig() {
		//si présent on récupère le paramétrage du fichier externe de paramétrage log4j
		return new XMLAppConfigBuilder()
				.withModules(getClass(), new Properties(), getManagersXmlFileName())
				.beginBoot().silently().endBoot().build();
	}

}
