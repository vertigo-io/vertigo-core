package io.vertigo.commons.peg;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class PegRulesTest {
	/**
	 * hi -> 0
	 * ho -> 1
	 * ha -> 2
	 */
	private final PegRule<PegChoice> choice = PegRules.choice(PegRules.term("hi"), PegRules.term("ho"), PegRules.term("ha"));
	private final PegRule sequence = PegRules.sequence(PegRules.term("hi"), PegRules.term("ho"), PegRules.term("ha"));
	private final PegRule oneOrMore = PegRules.oneOrMore(PegRules.term("hi"), true);
	private final PegRule zeroOrMore = PegRules.zeroOrMore(PegRules.term("hi"), true);
	private final PegRule skipBlanks = PegRules.skipBlanks("-*+");

	@Test
	public void choice() throws PegNoMatchFoundException {
		Assert.assertEquals(0, choice.parse("hi", 0).getValue().getChoiceIndex());
		Assert.assertEquals(1, choice.parse("ho", 0).getValue().getChoiceIndex());
		Assert.assertEquals(2, choice.parse("ha", 0).getValue().getChoiceIndex());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void choice2() throws PegNoMatchFoundException {
		choice.parse("hu", 0);
	}

	@Test
	public void sequence() throws PegNoMatchFoundException {
		Assert.assertEquals(Arrays.asList("hi", "ho", "ha"), sequence.parse("hihoha", 0).getValue());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void sequence2() throws PegNoMatchFoundException {
		Assert.assertEquals(Arrays.asList("hi", "ho", "ha"), sequence.parse("hiho", 0).getValue());
	}

	@Test
	public void optional() throws PegNoMatchFoundException {
		//option is not found => index =0
		Assert.assertEquals(0, PegRules.optional(choice).parse("hu", 0).getIndex());
		//option is found => index =2
		Assert.assertEquals(2, PegRules.optional(choice).parse("ha", 0).getIndex());
	}

	@Test
	public void oneOrMoreUntilTheEnd() throws PegNoMatchFoundException {
		Assert.assertEquals(Arrays.asList("hi", "hi", "hi"), oneOrMore.parse("hihihi", 0).getValue());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void oneOrMoreUntilTheEnd2() throws PegNoMatchFoundException {
		oneOrMore.parse("hihihiho", 0);
	}

	@Test
	public void zerOrMoreUntilTheEnd() throws PegNoMatchFoundException {
		Assert.assertEquals(0, zeroOrMore.parse("", 0).getIndex());
		Assert.assertEquals(Arrays.asList("hi", "hi", "hi"), zeroOrMore.parse("hihihi", 0).getValue());
	}

	@Test(expected = PegNoMatchFoundException.class)
	public void zeroOrMoreUntilTheEnd2() throws PegNoMatchFoundException {
		zeroOrMore.parse("hihihiho", 0);
	}

	@Test
	public void skipBlanks() throws PegNoMatchFoundException {
		Assert.assertEquals(10, skipBlanks.parse("+++****+++", 0).getIndex());
	}
}
