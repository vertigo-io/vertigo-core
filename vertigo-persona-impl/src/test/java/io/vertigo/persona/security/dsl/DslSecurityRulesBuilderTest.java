/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.security.dsl;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.persona.impl.security.SqlSecurityRuleTranslator;

/**
 * @author  npiedeloup
 */
public final class DslSecurityRulesBuilderTest {

	@Test
	public void testStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult, OtherAcceptedResult ...
				{ "ALL=${query}", "Test", "ALL=Test" }, //0
				{ "ALL=${query}", "'Test test2'", "ALL='Test test2'" }, //1
				//{ "ALL>${query}", "'Test'", "ALL like 'Test' || '%'" }, //2

		};
		testStringFixedQuery(testQueries);
	}

	int getPreferedResult() {
		return 3;
	}

	private void testStringFixedQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			final SqlSecurityRuleTranslator securityRuleTranslator = new SqlSecurityRuleTranslator()
					.withRule(testParam[0])
					.withCriteria(Collections.singletonMap("query", new String[] { testParam[1] }));
			final String result = securityRuleTranslator.toSql();
			final String expectedResult = testParam[Math.min(getPreferedResult(), testParam.length - 1)];
			Assert.assertEquals("Built query #" + i + " incorrect", expectedResult, result);
			i++;
		}
	}

}
