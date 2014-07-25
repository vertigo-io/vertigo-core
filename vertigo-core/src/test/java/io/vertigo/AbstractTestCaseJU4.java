package io.vertigo;

import io.vertigo.kernel.AppBuilder;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Container;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.lang.Option;

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
		final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder();
		configMe(componentSpaceConfigBuilder);
		Home.start(componentSpaceConfigBuilder.build());
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
		final Injector injector = new Injector();
		injector.injectMembers(this, getContainer());
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
	private Container getContainer() {
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
	protected static final void testDescription(final Manager manager) {
		if (manager instanceof Describable) {
			final List<ComponentInfo> componentInfos = Describable.class.cast(manager).getInfos();
			for (final ComponentInfo componentInfo : componentInfos) {
				Assert.assertNotNull(componentInfo);
			}
		}
	}

	/**
	 * Configuration des tests.
	 * @param componentSpaceConfigBuilder builder
	 */
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfigBuilder) {
		final AppBuilder appBuilder = new AppBuilder() //
				.withSilence(true) //
				.withComponentSpaceConfigBuilder(componentSpaceConfigBuilder) //
				.withXmlFileNames(getClass(), getManagersXmlFileName()); //
		if (getPropertiesFileName().isDefined()) {
			appBuilder.withEnvParams(getClass(), getPropertiesFileName().get());
		}
		appBuilder.toComponentSpaceConfigBuilder();
	}
}
