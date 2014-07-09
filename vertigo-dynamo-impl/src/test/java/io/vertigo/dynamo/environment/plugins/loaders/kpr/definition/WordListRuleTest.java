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
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.WordsRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public final class WordListRuleTest {
	private final WordsRule wordListRule = new WordsRule();

	@Test
	public void testList0() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse("[ ]", 0);
		final List<String> list = parser.get();
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testList1() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse("[BLEU, VerT, ROUGE ]", 0);
		final List<String> list = parser.get();
		Assert.assertEquals(3, list.size());
		Assert.assertTrue(list.contains("BLEU"));
		Assert.assertTrue(list.contains("VerT"));
		Assert.assertTrue(list.contains("ROUGE"));
	}

	@Test
	public void testList2() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse("[BLEU, VERT, ROUGE ]", 0);
		final List<String> list = parser.get();
		Assert.assertEquals(3, list.size());
		Assert.assertTrue(list.contains("BLEU"));
		Assert.assertTrue(list.contains("VERT"));
		Assert.assertTrue(list.contains("ROUGE"));
	}

	@Test(expected = Exception.class)
	public void testList3() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse(" [BLEU  ,	VERT,   ROUGE ,  Orange,] ", 0);
		final List<String> list = parser.get();
		Assert.fail("liste :" + list);
	}

	@Test(expected = Exception.class)
	public void testList4() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse(" [ , BLEU,VERT,   ROUGE ,  Violet] ", 0);
		final List<String> list = parser.get();
		Assert.fail("liste :" + list);
	}

	@Test
	public void testList5() throws NotFoundException {
		Parser<List<String>> parser = wordListRule.createParser();
		parser.parse("[BLEU ]", 0);
		final List<String> list = parser.get();
		Assert.assertEquals(1, list.size());
		Assert.assertTrue(list.contains("BLEU"));
	}
}
