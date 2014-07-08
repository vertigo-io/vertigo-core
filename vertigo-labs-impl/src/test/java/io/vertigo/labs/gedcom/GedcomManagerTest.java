package io.vertigo.labs.gedcom;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.labs.gedcom.GedcomManager;

import javax.inject.Inject;

import org.junit.Test;

/**
 */
public class GedcomManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private GedcomManager gedcomManager;

	@Test
	public void loadDatas() {
		gedcomManager.getAllIndividuals().size();
	}
}
