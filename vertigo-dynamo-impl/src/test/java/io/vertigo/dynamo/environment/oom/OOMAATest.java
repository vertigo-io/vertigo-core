package io.vertigo.dynamo.environment.oom;

import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.poweramc.core.OOMLoader;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test de lecture d'un OOM AutoAssociation.
 *
 * @author pchretien
 */
public class OOMAATest {
	private static final Logger LOGGER = Logger.getLogger(OOMAATest.class);
	private Map<String, OOMAssociation> map;

	@Before
	public void setUp() throws Exception {
		final File oomFile = TestUtil.getFile("data/AssociationAA.oom", getClass());
		final URL oomURL = oomFile.toURL();

		final OOMLoader loader = new OOMLoader(oomURL);
		map = new HashMap<>();
		for (final OOMAssociation associationOOM : loader.getAssociationOOMList()) {
			map.put(associationOOM.getCode(), associationOOM);
			LOGGER.trace("> code = " + associationOOM.getCode());
		}
		LOGGER.trace(">> nb ass.=" + loader.getAssociationOOMList().size());

	}

	@After
	public void tearDown() {
		map = null;
	}

	/*
	 * Conventions de nommage utilisées pour les tests ci dessous.
	 * - Relation de A vers B
	 * - Cardinalité notée 	1 ou n
	 * - Navigabilité notée v 
	 */

	/**
	 * Test d'une relation A1 - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1Bnv() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_1");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R1A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R1B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBnv() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_2");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R2A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R2B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBn() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_3");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R3A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R3B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnB1v() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_4");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R4A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R4B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_5");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R5A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R5B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1v() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_6");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R6A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R6B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBnv() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_7");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R7A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R7B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBnv() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_8");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R8A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R8B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBn() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_9");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R9A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R9B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBn() {
		final OOMAssociation associationOOM = map.get("CHI_CHI_10");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R10A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R10B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

}
