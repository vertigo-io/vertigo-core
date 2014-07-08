package io.vertigo.labs.france;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.labs.france.FranceManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public class FranceManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private FranceManager franceManager;

	@Test
	public final void testRegions() {
		Assert.assertEquals(27, franceManager.getRegions().size());
	}

	@Test
	public final void testRegion() {
		Assert.assertEquals("LORRAINE", franceManager.getRegion("41").getLabel().toUpperCase());
	}

	@Test
	public final void testDepartements() {
		Assert.assertEquals(101, franceManager.getDepartements().size());
	}

	@Test
	public final void testDepartement() {
		Assert.assertEquals("MEUSE", franceManager.getDepartement("55").getLabel().toUpperCase());
	}
}
