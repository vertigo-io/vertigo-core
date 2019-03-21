/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;

/**
 * Test de lecture d'un OOM.
 *
 * @author pchretien
 */
public class OOMParserTest extends AbstractTestCaseJU4 {
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "managers-test.xml", "resources-test-assoc.xml" };
	}

	/*
	 * Conventions de nommage utilisées pour les tests ci dessous.
	 * - Relation de A vers B
	 * - Cardinalité notée 	1 ou n
	 * - Navigabilité notée v
	 */
	private AssociationSimpleDefinition getAssociationSimpleDefinition(final String urn) {
		return getApp().getDefinitionSpace().resolve(urn, AssociationSimpleDefinition.class);
	}

	private AssociationNNDefinition getAssociationNNDefinition(final String urn) {
		return getApp().getDefinitionSpace().resolve(urn, AssociationNNDefinition.class);
	}

	/**
	* Test d'une relation A1 - Bnv.
	*/
	@Test
	public void testAssoctationA1Bnv() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_1");
		Assert.assertNotNull(association);
		/* "0..1" */
		Assert.assertFalse(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R1A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R1B", association.getAssociationNodeB().getRole());

		Assert.assertFalse(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());

	}

	/**
	 * Test d'une relation A1v - Bnv.
	 */
	@Test
	public void testAssoctationA1vBnv() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_2");
		/* "0..1" */
		Assert.assertFalse(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R2A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R2B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation A1v - Bn.
	 */
	@Test
	public void testAssoctationA1vBn() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_3");
		/* "0..1" */
		Assert.assertFalse(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R3A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R3B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertFalse(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - B1v.
	 */
	@Test
	public void testAssoctationAnB1v() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_4");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..1" */
		Assert.assertFalse(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R4A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R4B", association.getAssociationNodeB().getRole());

		Assert.assertFalse(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - B1.
	 */
	@Test
	public void testAssoctationAnvB1() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_5");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertFalse(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R5A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R5B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertFalse(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - B1v.
	 */
	@Test
	public void testAssoctationAnvB1v() {
		final AssociationSimpleDefinition association = getAssociationSimpleDefinition("A_CHA_CHI_6");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..1" */
		Assert.assertFalse(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R6A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R6B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - Bnv.
	 */
	@Test
	public void testAssoctationAnBnv() {
		final AssociationNNDefinition association = getAssociationNNDefinition("ANN_CHA_CHI_7");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R7A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R7B", association.getAssociationNodeB().getRole());

		Assert.assertFalse(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - Bnv.
	 */
	@Test
	public void testAssoctationAnvBnv() {
		final AssociationNNDefinition association = getAssociationNNDefinition("ANN_CHA_CHI_8");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R8A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R8B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertTrue(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - Bn.
	 */
	@Test
	public void testAssoctationAnBn() {
		final AssociationNNDefinition association = getAssociationNNDefinition("ANN_CHA_CHI_9");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R9A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R9B", association.getAssociationNodeB().getRole());

		Assert.assertFalse(association.getAssociationNodeA().isNavigable());
		Assert.assertFalse(association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - Bn.
	 */
	@Test
	public void testAssoctationAnvBn() {
		final AssociationNNDefinition association = getAssociationNNDefinition("ANN_CHA_CHI_10");
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeA().isMultiple());
		Assert.assertFalse(association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertTrue(association.getAssociationNodeB().isMultiple());
		Assert.assertFalse(association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R10A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R10B", association.getAssociationNodeB().getRole());

		Assert.assertTrue(association.getAssociationNodeA().isNavigable());
		Assert.assertFalse(association.getAssociationNodeB().isNavigable());
	}
}
