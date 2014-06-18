package io.vertigo.studio.reporting;

import io.vertigo.AbstractTestCaseJU4;

import javax.inject.Inject;

import org.junit.Test;

/**
 * @author pchretien, npiedeloup
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
