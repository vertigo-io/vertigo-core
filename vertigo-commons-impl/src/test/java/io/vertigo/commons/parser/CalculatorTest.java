package io.vertigo.commons.parser;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {
	private static final Rule<Integer> MAIN = new CalculatorRule();

	@Test
	public void test() throws NotFoundException {
		final Parser<Integer> parser = MAIN.createParser();
		//--
		parser.parse("2*3", 0);
		Assert.assertEquals(6, parser.get().intValue());
		//--
		parser.parse("2 + 3", 0);
		Assert.assertEquals(5, parser.get().intValue());
		//--
		parser.parse("121 /11", 0);
		Assert.assertEquals(11, parser.get().intValue());
	}

	@Test(expected = NotFoundException.class)
	public void testFail() throws NotFoundException {
		final Parser<Integer> parser = MAIN.createParser();
		//--
		parser.parse("2 $ 3", 0); //l'op�rateur  $ n'existe pas 
		Assert.fail();
	}
}
