package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.PackageRule;

import org.junit.Assert;
import org.junit.Test;

public class PackageRuleTest {
	private static final PackageRule packageRule = new PackageRule();

	@Test
	public void test1() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("package io.vertigo.dynamock;", 0);
		Assert.assertEquals("io.vertigo.dynamock", parser.get());
	}

	@Test
	public void test2() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("package io.vertigo.dynamock.avecpoint;", 0);
		Assert.assertEquals("io.vertigo.dynamock.avecpoint", parser.get());
	}

	@Test
	public void test3() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("package io.vertigo.dynamock;", 0);
		Assert.assertEquals("io.vertigo.dynamock", parser.get());
	}

	@Test(expected = Exception.class)
	public void test4() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("packageio.vertigo.dynamock;", 0);
		Assert.fail("package : " + parser.get());
	}

	@Test(expected = Exception.class)
	public void test5() throws NotFoundException {
		Parser<String> parser = packageRule.createParser();
		parser.parse("package io.vertigo.dynamock io.vertigo.dynamock;", 0);
		Assert.fail("package : " + parser.get());
	}
}
