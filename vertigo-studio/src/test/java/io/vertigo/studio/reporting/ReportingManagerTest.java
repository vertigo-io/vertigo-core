package io.vertigo.studio.reporting;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.studio.reporting.ReportingManager;

import javax.inject.Inject;

import org.junit.Test;

/**
 * @author pchretien, npiedeloup
 * @version $Id: ReportingManagerTest.java,v 1.2 2013/10/22 10:59:26 pchretien Exp $
 */
public final class ReportingManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private ReportingManager reportingManager;

	/**
	 * On lance l'analyse et la création de rapport.
	 */
	@Test
	public void testAnalyze() {
		reportingManager.analyze();
	}
}
