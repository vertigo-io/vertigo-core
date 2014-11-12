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
package io.vertigo.dynamo.environment.oom;

import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.plugins.environment.loaders.TagAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.TagLoader;
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
 * Test de lecture d'un OOM.
 *
 * @author pchretien
 */
public class OOMTest {
	private static final Logger LOGGER = Logger.getLogger(OOMTest.class);
	private Map<String, TagAssociation> map;

	@Before
	public void setUp() throws Exception {
		final File oomFile = TestUtil.getFile("data/Associations.oom", getClass());

		final URL oomURL = oomFile.toURL();
		final TagLoader loader = new OOMLoader(oomURL);
		map = new HashMap<>();
		for (final TagAssociation associationOOM : loader.getTagAssociations()) {
			map.put(associationOOM.getCode(), associationOOM);
			LOGGER.trace("> code = " + associationOOM.getCode());
		}
		LOGGER.trace(">> nb ass.=" + loader.getTagAssociations().size());

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
	 */
	@Test
	public void testAssoctationA1Bnv() {
		final TagAssociation associationOOM = map.get("CHA_CHI_1");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R1A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R1B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bnv.
	 */
	@Test
	public void testAssoctationA1vBnv() {
		final TagAssociation associationOOM = map.get("CHA_CHI_2");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R2A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R2B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bn.
	 */
	@Test
	public void testAssoctationA1vBn() {
		final TagAssociation associationOOM = map.get("CHA_CHI_3");
		Assert.assertEquals("0..1", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R3A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R3B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - B1v.
	 */
	@Test
	public void testAssoctationAnB1v() {
		final TagAssociation associationOOM = map.get("CHA_CHI_4");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R4A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R4B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1.
	 */
	@Test
	public void testAssoctationAnvB1() {
		final TagAssociation associationOOM = map.get("CHA_CHI_5");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R5A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R5B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1v.
	 */
	@Test
	public void testAssoctationAnvB1v() {
		final TagAssociation associationOOM = map.get("CHA_CHI_6");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..1", associationOOM.getMultiplicityB());

		Assert.assertEquals("R6A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R6B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bnv.
	 */
	@Test
	public void testAssoctationAnBnv() {
		final TagAssociation associationOOM = map.get("CHA_CHI_7");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R7A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R7B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bnv.
	 */
	@Test
	public void testAssoctationAnvBnv() {
		final TagAssociation associationOOM = map.get("CHA_CHI_8");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R8A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R8B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(true, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bn.
	 */
	@Test
	public void testAssoctationAnBn() {
		final TagAssociation associationOOM = map.get("CHA_CHI_9");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R9A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R9B", associationOOM.getRoleLabelB());

		Assert.assertEquals(false, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bn.
	 */
	@Test
	public void testAssoctationAnvBn() {
		final TagAssociation associationOOM = map.get("CHA_CHI_10");
		Assert.assertEquals("0..*", associationOOM.getMultiplicityA());
		Assert.assertEquals("0..*", associationOOM.getMultiplicityB());

		Assert.assertEquals("R10A", associationOOM.getRoleLabelA());
		Assert.assertEquals("R10B", associationOOM.getRoleLabelB());

		Assert.assertEquals(true, associationOOM.isNavigableA());
		Assert.assertEquals(false, associationOOM.isNavigableB());
	}
}
