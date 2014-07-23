package io.vertigo;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Container;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.xml.XMLModulesLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
	private static boolean homeStarted;

	/**
	 * Affecte homeStarted.
	 *
	 * @param homeStarted la valeur homeStarted à affecter
	 */
	private static synchronized void setHomeStarted(final boolean homeStarted) {
		AbstractTestCaseJU4.homeStarted = homeStarted;
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

	private void startHome() {
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder().withSilence(true);
		// final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new
		// ComponentSpaceConfigBuilder().withRestEngine(new GrizzlyRestEngine(8086)).withSilence(true);
		configMe(componentSpaceConfigBuilder);
		Home.start(componentSpaceConfigBuilder.build());
		setHomeStarted(true);
	}

	private void stopHome() {
		Home.stop();
		setHomeStarted(false);
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
	 *
	 * @param componentSpaceConfiguilder builder
	 */
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		for (final URL url : loadManagersXml()) {
			componentSpaceConfiguilder.withLoader(new XMLModulesLoader(url, loadProperties()));
		}
	}

	private URL[] loadManagersXml() {
		final URL[] urls = new URL[getManagersXmlFileName().length];
		int i = 0;
		for (final String managersXmlFileName : getManagersXmlFileName()) {
			urls[i] = getClass().getResource(managersXmlFileName);
			Assertion.checkNotNull(urls[i], "file configuration '{0}' not found", managersXmlFileName);
			i++;
		}
		return urls;
	}

	private Properties loadProperties() {
		try {
			final Option<String> propertiesName = getPropertiesFileName();
			final Properties properties = new Properties();
			if (propertiesName.isDefined()) {
				try (final InputStream in = createURL(propertiesName.get()).openStream()) {
					properties.load(in);
				}
			}
			return properties;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retourne l'URL correspondant au nom du fichier dans le classPath.
	 *
	 * @param fileName Nom du fichier
	 * @return URN non null
	 */
	private URL createURL(final String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		// ---------------------------------------------------------------------
		try {
			return new URL(fileName);
		} catch (final MalformedURLException e) {
			// Si fileName non trouvé, on recherche dans le classPath
			final URL url = getClass().getResource(fileName);
			Assertion.checkNotNull(url, "Impossible de récupérer le fichier [" + fileName + "]");
			return url;
		}
	}

}
