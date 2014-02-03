package io.vertigo.commons.analytics;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.analytics.AnalyticsManager;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Cas de Test JUNIT de l'API Analytics.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: AnalyticsTest.java,v 1.2 2013/10/22 10:46:21 pchretien Exp $
 */
public final class AnalyticsManagerTest extends AbstractTestCaseJU4 {

	/** Base de donn�es g�rant les articles envoy�s dans une commande. */
	private static final String PROCESS_TYPE = "ARTICLE";

	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private AnalyticsManager analyticsManager;

	//-------------------------------------------------------------------------

	/**
	 * Test simple avec deux compteurs. 
	 * Test sur l'envoi de 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10�.
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
	 * Test pour v�rifier que l'on peut se passer des processus si et seulement si le mode Analytics est d�sactiv�.
	 */
	@Test
	public void testNoProcess() {
		analyticsManager.getAgent().incMeasure("POIDS", 25);
		//Dans le cas du dummy �a doit passer
	}

	/**
	 * M�me test apr�s d�sactivation.
	 */
	@Test
	public void testOff() {
		test1000Articles();
	}

	/**
	 * Test de r�cursivit�. 
	 * Test sur l'envoi de 1000 commandes contenant chacune 1000 articles d'un poids de 25 kg. 
	 * Chaque article coute 10�. 
	 * Les frais d'envoi sont de 5�.
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
