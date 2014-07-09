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
