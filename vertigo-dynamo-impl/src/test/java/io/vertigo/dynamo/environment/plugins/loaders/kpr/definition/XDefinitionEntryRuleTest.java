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
package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.XDefinitionEntryRule;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public final class XDefinitionEntryRuleTest {
	private static final XDefinitionEntryRule MAIN = new XDefinitionEntryRule(Arrays.asList(new String[] { "myFirstProperty", "myLastProperty" }));

	@Test
	public void test0() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myFirstProperty : [BLEU ], non reconnu";
		final int end = parser.parse(text, 0);
		final XDefinitionEntry xDefinitionEntry = parser.get();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(1, xDefinitionEntry.getDefinitionKeys().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionKeys().contains("BLEU"));
		Assert.assertEquals(text.length() - " non reconnu".length(), end);
	}

	@Test
	public void test1() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myFirstProperty : [BLEU, VerT, ROUGE, T_REX ], non reconnu";
		final int end = parser.parse(text, 0);
		final XDefinitionEntry xDefinitionEntry = parser.get();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(4, xDefinitionEntry.getDefinitionKeys().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionKeys().contains("VerT"));
		Assert.assertEquals(text.length() - " non reconnu".length(), end);

	}

	@Test
	public void test2() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myLastProperty : [ ],";
		final int end = parser.parse(text, 0);
		final XDefinitionEntry xDefinitionEntry = parser.get();
		Assert.assertEquals("myLastProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(0, xDefinitionEntry.getDefinitionKeys().size());
		Assert.assertEquals(text.length(), end);
	}

	@Test
	public void test3() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myFirstProperty    :    [BLEU,VerT,    ROUGE    ]";
		final int end = parser.parse(text, 0);
		final XDefinitionEntry xDefinitionEntry = parser.get();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(3, xDefinitionEntry.getDefinitionKeys().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionKeys().contains("VerT"));
		Assert.assertEquals(text.length(), end);
	}

	@Test
	public void test4() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myFirstProperty : BLEU,";
		final int end = parser.parse("myFirstProperty : BLEU,", 0);
		final XDefinitionEntry xDefinitionEntry = parser.get();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(1, xDefinitionEntry.getDefinitionKeys().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionKeys().contains("BLEU"));
		Assert.assertEquals(text.length(), end);
	}

	@Test(expected = NotFoundException.class)
	public void testFail1() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myLastProperty : [BLEU;"; //on ne ferme pas l'accolade
		/*final int end =*/parser.parse(text, 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testFail2() throws NotFoundException {
		final Parser<XDefinitionEntry> parser = MAIN.createParser();
		//---
		final String text = "myUnknownProperty : BLEU"; //on positionne un nom erroné de propriété
		/*final int end =*/parser.parse(text, 0);
		Assert.fail();
	}
}
