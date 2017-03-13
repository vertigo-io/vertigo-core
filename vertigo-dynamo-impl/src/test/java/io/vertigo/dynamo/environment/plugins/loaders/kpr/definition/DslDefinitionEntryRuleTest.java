/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.commons.peg.PegResult;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslDefinitionEntryRule;

public final class DslDefinitionEntryRuleTest {
	private static final DslDefinitionEntryRule MAIN = new DslDefinitionEntryRule(Arrays.asList(new String[] { "myFirstProperty", "myLastProperty" }));

	@Test
	public void test0() throws PegNoMatchFoundException {
		final String text = "myFirstProperty : [BLEU ], non reconnu";
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);
		final DslDefinitionEntry xDefinitionEntry = cursor.getValue();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(1, xDefinitionEntry.getDefinitionNames().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionNames().contains("BLEU"));
		Assert.assertEquals(text.length() - " non reconnu".length(), cursor.getIndex());
	}

	@Test
	public void test1() throws PegNoMatchFoundException {
		final String text = "myFirstProperty : [BLEU, VerT, ROUGE, T_REX ], non reconnu";
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);
		final DslDefinitionEntry xDefinitionEntry = cursor.getValue();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(4, xDefinitionEntry.getDefinitionNames().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionNames().contains("VerT"));
		Assert.assertEquals(text.length() - " non reconnu".length(), cursor.getIndex());

	}

	@Test
	public void test2() throws PegNoMatchFoundException {
		final String text = "myLastProperty : [ ],";
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);

		final DslDefinitionEntry xDefinitionEntry = cursor.getValue();
		Assert.assertEquals("myLastProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(0, xDefinitionEntry.getDefinitionNames().size());
		Assert.assertEquals(text.length(), cursor.getIndex());
	}

	@Test
	public void test3() throws PegNoMatchFoundException {
		final String text = "myFirstProperty    :    [BLEU,VerT,    ROUGE    ]";
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);
		final DslDefinitionEntry xDefinitionEntry = cursor.getValue();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(3, xDefinitionEntry.getDefinitionNames().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionNames().contains("VerT"));
		Assert.assertEquals(text.length(), cursor.getIndex());
	}

	@Test
	public void test4() throws PegNoMatchFoundException {
		final String text = "myFirstProperty : BLEU,";
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);
		final DslDefinitionEntry xDefinitionEntry = cursor.getValue();
		Assert.assertEquals("myFirstProperty", xDefinitionEntry.getFieldName());
		Assert.assertEquals(1, xDefinitionEntry.getDefinitionNames().size());
		Assert.assertTrue(xDefinitionEntry.getDefinitionNames().contains("BLEU"));
		Assert.assertEquals(text.length(), cursor.getIndex());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testFail1() throws PegNoMatchFoundException {
		final String text = "myLastProperty : [BLEU;";
		//on ne ferme pas l'accolade
		final PegResult<DslDefinitionEntry> cursor = MAIN
				.parse(text, 0);
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void testFail2() throws PegNoMatchFoundException {
		final String text = "myUnknownProperty : BLEU";
		//on positionne un nom erroné de propriété
		MAIN.parse(text, 0);
	}
}
