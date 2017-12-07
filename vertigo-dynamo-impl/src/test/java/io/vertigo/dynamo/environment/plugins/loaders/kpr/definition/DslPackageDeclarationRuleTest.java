/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.commons.peg.PegResult;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslPackageDeclarationRule;

public final class DslPackageDeclarationRuleTest {
	private static final DslPackageDeclarationRule PACKAGE_DECLARATION_RULE = new DslPackageDeclarationRule();

	@Test
	public void testExpression() throws PegNoMatchFoundException {
		final PegResult<String> result = PACKAGE_DECLARATION_RULE
				.parse("package io.vertigo  xxxx", 0);
		Assert.assertEquals("io.vertigo", result.getValue());
		Assert.assertEquals("package io.vertigo".length(), result.getIndex());
	}

	@Test(expected = Exception.class)
	public void testMalFormedExpression() throws PegNoMatchFoundException {
		final PegResult<String> result = PACKAGE_DECLARATION_RULE
				.parse("packageio.vertigo", 0);//<-- en exception is excpected here
		Assert.assertNotNull(result);
	}

	@Test(expected = Exception.class)
	public void testMalFormedExpression2() throws PegNoMatchFoundException {
		final PegResult<String> cursor = PACKAGE_DECLARATION_RULE
				.parse("  packageio.vertigo", 0);//<-- en exception is excpected here
		Assert.assertNotNull(cursor);
	}
}
