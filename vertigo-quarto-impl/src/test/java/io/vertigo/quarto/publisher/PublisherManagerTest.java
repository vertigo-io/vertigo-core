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
package io.vertigo.quarto.publisher;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.quarto.publisher.metamodel.PublisherDataDefinition;
import io.vertigo.quarto.publisher.metamodel.PublisherField;
import io.vertigo.quarto.publisher.metamodel.PublisherNodeDefinition;
import io.vertigo.quarto.publisher.metamodel.PublisherNodeDefinitionBuilder;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.quarto.x.publisher.PublisherDataUtil;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author npiedeloup
 */
public final class PublisherManagerTest extends AbstractTestCaseJU4 {
	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	private static void registerDefinition(final PublisherDataDefinition publisherDataDefinition) {
		Home.getDefinitionSpace().put(publisherDataDefinition, PublisherDataDefinition.class);
	}

	/**
	 * Créer une Définition simple avec 1 bool, 1 string.
	 */
	@Test
	public final void testDefinitionSimple() {
		final PublisherNodeDefinition rootNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_1", rootNodeDefinition);
		// --------------------
		registerDefinition(publisherDataDefinition);
		// --------------------
		final PublisherData publisherData = createPublisherData("PU_TEST_1");
		// on teste juste.
		log.trace(asString(publisherData.getDefinition()));
	}

	/**
	 * Test le nommage d'une definition.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testDefinitionNomMinuscules() {
		final PublisherNodeDefinition rootDefinition = createNodeDefinition();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("pu_test", rootDefinition);
		Home.getDefinitionSpace().put(publisherDataDefinition, PublisherDataDefinition.class);
	}

	/**
	 * Test le nommage d'une definition.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testDefinitionNomAvecPoint() {
		final PublisherNodeDefinition rootDefinition = createNodeDefinition();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST.TOTO", rootDefinition);
		Home.getDefinitionSpace().put(publisherDataDefinition, PublisherDataDefinition.class);
		nop(publisherDataDefinition);
	}

	/**
	 * Test le nommage d'une definition.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testDefinitionNomAvecArobase() {
		final PublisherNodeDefinition rootDefinition = createNodeDefinition();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST@TOTO", rootDefinition);
		Home.getDefinitionSpace().put(publisherDataDefinition, PublisherDataDefinition.class);
		nop(publisherDataDefinition);
	}

	/**
	 * Test le nommage d'une definition.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testDefinitionNom1() {
		final PublisherNodeDefinition rootDefinition = createNodeDefinition();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_AZERTYUIOPQSDFGHJKLMWXCVBN_AZERTYUIOPQSDFGHJKLMWXCVBN", rootDefinition);
		Home.getDefinitionSpace().put(publisherDataDefinition, PublisherDataDefinition.class);
		nop(publisherDataDefinition);
	}

	private static PublisherNodeDefinition createNodeDefinition() {
		return new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();
	}

	/**
	 * Test l'enregistrement de deux définitions avec le même nom.
	 */
	@Test
	public final void testDefinitionDoubleRegister() {
		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST", publisherNodeDefinition);
		registerDefinition(publisherDataDefinition);

		final PublisherDataDefinition publisherDataDefinition2 = new PublisherDataDefinition("PU_TEST", publisherNodeDefinition);
		try {
			registerDefinition(publisherDataDefinition2);
			Assert.fail();
		} catch (final IllegalArgumentException a) {
			// succes
		}
	}

	/**
	 * Test le nommage d'une definition.
	 */
	@Test
	public final void testDefinitionFieldName() {
		final PublisherNodeDefinitionBuilder rootDefinitionBuilder = new PublisherNodeDefinitionBuilder();

		try {
			rootDefinitionBuilder.withStringField("testString");
			Assert.fail();
		} catch (final IllegalArgumentException a) {
			// succes
		}
		try {
			rootDefinitionBuilder.withBooleanField("TEST_BOOLEAN.TOTO");
			Assert.fail();
		} catch (final IllegalArgumentException a) {
			// succes
		}
		try {
			rootDefinitionBuilder.withImageField("TEST_BOOLEAN@TOTO");
			Assert.fail();
		} catch (final IllegalArgumentException a) {
			// succes
		}
		try {
			rootDefinitionBuilder.withStringField("TEST_BOOLEANAZERTYUIOPQSDFGHJKLMWXCVBN_AZERTYUIOPQSDFGHJKLMWXCVBN");
			Assert.fail();
		} catch (final IllegalArgumentException a) {
			// succes
		}
	}

	/**
	 * Test l'enregistrement de deux définitions avec le même nom.
	 */
	@Test(expected = IllegalArgumentException.class)
	public final void testDefinitionFieldDoubleRegister() {
		final PublisherNodeDefinitionBuilder rootDefinitionBuilder = new PublisherNodeDefinitionBuilder()//
				.withBooleanField("TEST_STRING")//
				.withStringField("TEST_STRING");
		final PublisherNodeDefinition rootDefinition = rootDefinitionBuilder.build();
		nop(rootDefinition);
	}

	/**
	 * Test l'enregistrement de deux définitions avec le même nom.
	 */
	@Test
	public final void testDefinitionInfinitLoopRegister() {
		try {
			final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
					.withBooleanField("TEST_BOOLEAN")//
					.build();
			final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_1_BIS", publisherNodeDefinition);
			registerDefinition(publisherDataDefinition);

			/*final PublisherDataNodeDefinition subDefinition = publisherDataDefinitionFactory.createPublisherDataNodeDefinitionBuilder() //
					.registerStringField("TEST_STRING")//
					.registerNodeField("TEST_DATA", rootDefinition)//
					.toNodeDefinition();
			 */

			final PublisherData publisherData = createPublisherData("PU_TEST_1_BIS");
			log.trace(asString(publisherData.getDefinition()));
			Assert.fail("Il est possible de créer une structure cyclique.");
		} catch (final IllegalArgumentException a) {
			// succes
		}
	}

	/**
	 * Crée une Définition simple avec 1 bool, 1 string et un sous objet.
	 */
	@Test
	public final void testDefinitionWithData() {
		final PublisherNodeDefinition subDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.withNodeField("TEST_DATA", subDefinition)//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_2", publisherNodeDefinition);
		registerDefinition(publisherDataDefinition);

		final PublisherData publisherData = createPublisherData("PU_TEST_2");
		log.trace(asString(publisherData.getDefinition()));
	}

	/**
	 * Créer une Définition avec 1 bool, 1 string et un sous objet et une liste
	 * d'objets.
	 */
	@Test
	public final void testDefinitionWithDataAndList() {
		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN")//
				.withStringField("TEST_STRING")//
				.withNodeField("TEST_DATA", subDefinition1)//
				.withListField("TEST_LIST", subDefinition3)//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_3", publisherNodeDefinition);

		registerDefinition(publisherDataDefinition);

		final PublisherData publisherData = createPublisherData("PU_TEST_3");
		log.trace(asString(publisherData.getDefinition()));
	}

	/**
	 * Créer une Définition avec 1 bool, 1 string, un sous objet, une liste
	 * d'objets et une image.
	 */
	@Test
	public final void testDefinitionWithDataImageAndList() {
		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN")//
				.withStringField("TEST_STRING")//
				.withNodeField("TEST_DATA", subDefinition1)//
				.withListField("TEST_LIST", subDefinition3)//
				.withImageField("TEST_IMAGE")//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_4", publisherNodeDefinition);

		registerDefinition(publisherDataDefinition);

		final PublisherData publisherData = createPublisherData("PU_TEST_4");
		log.trace(asString(publisherData.getDefinition()));
	}

	/**
	 * Crée une Définition hierarchique avec 1 bool, 1 string et un sous objet.
	 * contenant à et une liste d’objets contenant à
	 */
	@Test
	public final void testDefinitionWithHierachy() {

		final PublisherNodeDefinition subDefinition2 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition subDefinition1 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN") //
				.withStringField("TEST_STRING")//
				.withNodeField("TEST_DATA", subDefinition2)//
				.build();

		final PublisherNodeDefinition subDefinition4 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN")//
				.withStringField("TEST_STRING")//
				.build();

		final PublisherNodeDefinition subDefinition3 = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN")//
				.withStringField("TEST_STRING")//
				.withListField("TEST_LIST", subDefinition4)//
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("TEST_BOOLEAN")//
				.withStringField("TEST_STRING")//
				.withNodeField("TEST_DATA", subDefinition1)//
				.withListField("TEST_LIST", subDefinition3)//
				.build();
		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_5", publisherNodeDefinition);
		registerDefinition(publisherDataDefinition);

		final PublisherData publisherData = createPublisherData("PU_TEST_5");
		log.trace(asString(publisherData.getDefinition()));
	}

	/**
	 * Crée une Définition hierarchique complexe sur le model des enquetes.
	 */
	@Test
	public final void testDefinitionEnquete() {
		final PublisherNodeDefinition ville = new PublisherNodeDefinitionBuilder() //
				.withStringField("NOM")//
				.withStringField("CODE_POSTAL")//
				.build();

		final PublisherNodeDefinition address = new PublisherNodeDefinitionBuilder() //
				.withStringField("RUE")//
				.withNodeField("VILLE", ville)//
				.build();

		final PublisherNodeDefinition enqueteur = new PublisherNodeDefinitionBuilder() //
				.withStringField("NOM")//
				.withStringField("PRENOM")//
				.withNodeField("ADRESSE_RATACHEMENT", address)//
				.build();

		final PublisherNodeDefinition misEnCause = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("SI_HOMME")//
				.withStringField("NOM")//
				.withStringField("PRENOM")//
				.withListField("ADRESSES_CONNUES", address)//
				.build();

		final PublisherNodeDefinition publisherNodeDefinition = new PublisherNodeDefinitionBuilder() //
				.withBooleanField("ENQUETE_TERMINEE")//
				.withStringField("CODE_ENQUETE")//
				.withNodeField("ENQUETEUR", enqueteur)//
				.withListField("MIS_EN_CAUSE", misEnCause)//
				.withStringField("FAIT")//
				.withBooleanField("SI_GRAVE")//
				.build();

		final PublisherDataDefinition publisherDataDefinition = new PublisherDataDefinition("PU_TEST_ENQUETE", publisherNodeDefinition);
		registerDefinition(publisherDataDefinition);

		final PublisherData publisherData = createPublisherData("PU_TEST_ENQUETE");
		// on test juste.
		Assert.assertEquals(ENQUETE_DEF, asString(publisherData.getDefinition()));
	}

	private static final String ENQUETE_DEF = "=== PU_TEST_ENQUETE =====================================\nBoolean:ENQUETE_TERMINEE\nString:CODE_ENQUETE\nNode:ENQUETEUR\n    String:NOM\n    String:PRENOM\n    Node:ADRESSE_RATACHEMENT\n        String:RUE\n        Node:VILLE\n            String:NOM\n            String:CODE_POSTAL\nList:MIS_EN_CAUSE\n    Boolean:SI_HOMME\n    String:NOM\n    String:PRENOM\n    List:ADRESSES_CONNUES\n        String:RUE\n        Node:VILLE\n            String:NOM\n            String:CODE_POSTAL\nString:FAIT\nBoolean:SI_GRAVE\n------------------------------------------------------------------------------";

	/**
	 * Créer une Définition hierarchique complexe sur le model des enquetes
	 */
	@Test
	public final void testKspDefinitionEnquete() {
		final PublisherData publisherData = createPublisherData("PU_ENQUETE"); // on test juste.
		log.trace(asString(publisherData.getDefinition()));
	}

	@Test
	public final void testPublisherNodeGenerator() {
		log.trace(PublisherDataUtil.generatePublisherNodeDefinitionAsKsp("DT_ENQUETE", "DT_ENQUETEUR"));
	}

	private static PublisherData createPublisherData(final String definitionName) {
		final PublisherDataDefinition publisherDataDefinition = Home.getDefinitionSpace().resolve(definitionName, PublisherDataDefinition.class);
		Assert.assertNotNull(publisherDataDefinition);

		final PublisherData publisherData = new PublisherData(publisherDataDefinition);
		Assert.assertNotNull(publisherData);

		return publisherData;
	}

	private static String asString(final PublisherDataDefinition publisherDataDefinition) {
		final StringBuilder sb = new StringBuilder();
		sb.append("=== ").append(publisherDataDefinition.getName()).append(" =====================================");

		concatString(publisherDataDefinition.getRootNodeDefinition(), sb, "\n");
		sb.append("\n------------------------------------------------------------------------------");
		return sb.toString();
	}

	private static void concatString(final PublisherNodeDefinition nodeDefinition, final StringBuilder sb, final String padding) {
		for (final PublisherField field : nodeDefinition.getFields()) {
			sb.append(padding);
			sb.append(field.getFieldType().name());
			sb.append(":");
			sb.append(field.getName());
			if (field.getNodeDefinition().isDefined()) {
				concatString(field.getNodeDefinition().get(), sb, padding + "    ");
			}
		}
	}
}
