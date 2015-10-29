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

import io.vertigo.boot.xml.XMLAppConfigBuilder;
import io.vertigo.core.App;
import io.vertigo.core.Home;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.lang.Component;
import io.vertigo.lang.Describable;

import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Classe parente de tous les TNR associés à vertigo.
 *
 * @author jmforhan
 */
public abstract class AbstractTestCaseJU4 {
	private App app;

	/**
	 * Set up de l'environnement de test.
	 *
	 * @throws Exception exception
	 */
	@Before
	public final void setUp() throws Exception {
		app = new App(buildAppConfig());
		// On injecte les comosants sur la classe de test.
		Injector.injectMembers(this, Home.getApp().getComponentSpace());
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
				Assert.assertNotNull(componentInfo);
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
