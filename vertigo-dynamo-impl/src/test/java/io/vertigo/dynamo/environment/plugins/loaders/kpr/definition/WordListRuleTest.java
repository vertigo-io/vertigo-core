package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.WordListRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public final class WordListRuleTest {
	private final WordListRule wordListRule = new WordListRule();

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
