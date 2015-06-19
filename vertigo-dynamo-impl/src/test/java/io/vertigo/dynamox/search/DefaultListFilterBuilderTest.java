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
package io.vertigo.dynamox.search;

import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 */
public class DefaultListFilterBuilderTest {

	@Test
	public void testStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test", "ALL:(Test)" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query*#", "Test", "ALL:(Test*)" }, //2
				{ "ALL:#query*#", "Test test2", "ALL:(Test* test2*)" }, //3
				{ "ALL:#+query#", "Test", "ALL:(+Test)" }, //4
				{ "ALL:#+query#", "Test test2", "ALL:(+Test +test2)" }, //5
				{ "+ALL:#query#", "Test", "+ALL:(Test)" }, //6
				{ "+ALL:#query#", "Test test2", "+ALL:(Test test2)" }, //7
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringGlobalModifierQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test", "ALL:(Test)" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query#*", "Test", "ALL:(Test)*" }, //2
				{ "ALL:#query#*", "Test test2", "ALL:(Test test2)*" }, //3
				{ "ALL:+#query#", "Test", "ALL:+(Test)" }, //4
				{ "ALL:+#query#", "Test test2", "ALL:+(Test test2)" }, //5
				{ "-ALL:+#query#*", "Test", "-ALL:+(Test)*" }, //6
				{ "-ALL:+#query#*", "Test test2", "-ALL:+(Test test2)*" }, //7
				{ "-ALL:+#query#*", "Test AND (test2 OR test3)", "-ALL:+(Test AND (test2 OR test3))*" }, //8
		};
		testStringFixedQuery(testQueries[8]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringAdvancedQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test or test2", "ALL:(Test or test2)" }, //0
				{ "ALL:#query#", "Test and test2", "ALL:(Test and test2)" }, //1
				{ "ALL:#query#", "Test OR test2", "ALL:(Test OR test2)" }, //2
				{ "ALL:#query#", "Test AND test2", "ALL:(Test AND test2)" }, //3
				{ "ALL:#query#", "Test AND (test2 OR test3)", "ALL:(Test AND (test2 OR test3))" }, //4
				{ "ALL:#query*#", "Test AND test2", "ALL:(Test* AND test2*)" }, //5
				{ "ALL:#query*#", "Test AND (test2 OR test3)", "ALL:(Test* AND (test2* OR test3*))" }, //6
				{ "ALL:#+query*#", "Test AND (test2 OR test3)", "ALL:(+Test* AND (+test2* OR +test3*))" }, //7
				{ "+ALL:#query#", "Test or test2", "+ALL:(Test or test2)" }, //8
				{ "ALL:#+query~#", "Test AND (test2 OR test3)", "ALL:(+Test~ AND (+test2~ OR +test3~))" }, //9
				{ "ALL:#+query~1#", "Test AND (test2 OR test3)", "ALL:(+Test~1 AND (+test2~1 OR +test3~1))" }, //10
				{ "ALL:#+query#", "Test AND (test2^2 OR test3)", "ALL:(+Test AND (+test2^2 OR +test3))" }, //11
				{ "ALL:#+query^2#", "Test AND (test2 OR test3)", "ALL:(+Test^2 AND (+test2^2 OR +test3^2))" }, //12
				{ "ALL:#+query#^2", "Test AND (test2 OR test3)", "ALL:(+Test AND (+test2 OR +test3))^2" }, //13
				{ "ALL:#+query*#", "Test, test2, test3", "ALL:(+Test*, +test2*, +test3*)" }, //14
				{ "ALL:#query# +YEAR:[2000 to 2005]", "Test AND (test2 OR test3)", "ALL:(Test AND (test2 OR test3)) +YEAR:[2000 to 2005]" }, //15
		};
		testStringFixedQuery(testQueries[11]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringOverridedFieldQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "OTHER:Test", "OTHER:(Test)" }, //0
				{ "ALL:#+query*#", "OTHER:Test", "OTHER:(Test)" }, //1
				{ "ALL:#query#", "OTHER:Test test2", "OTHER:(Test) ALL:(test2)" }, //2
				{ "ALL:#query#", "Test OTHER:test2", "ALL:(Test) OTHER:(test2)" }, //3
				{ "ALL:#query#", "OTHER:Test test2 test3", "OTHER:(Test) ALL:(test2 test3)" }, //4
				{ "ALL:#query#", "Test OTHER:test2 test3", "ALL:(Test) OTHER:(test2) ALL:(test3)" }, //5
				{ "ALL:#query#", "Test test2 OTHER:test3", "ALL:(Test test2) OTHER:(test3)" }, //6
				{ "ALL:#+query*#", "Test test2 OTHER:test3", "ALL:(+Test* +test2*) OTHER:(test3)" }, //7
				{ "+ALL:#query#", "Test test2 OTHER:test3", "+ALL:(Test test2) OTHER:(test3)" }, //8
				{ "ALL:#+query*#", "Test OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)" }, //9
				{ "+ALL:#query#", "Test OTHER:(test2 test3)", "+ALL:(Test) OTHER:(test2 test3)" }, //10
				{ "ALL:#query#", "Test +OTHER:(test2 test3)", "ALL:(Test) +OTHER:(test2 test3)" }, //11
				{ "ALL:#+query*#", "Test test2~", "ALL:(+Test* +test2~)" }, //12
		};
		testStringFixedQuery(testQueries[2]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringOverridedModifierQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "+Test", "ALL:(+Test)" }, //0
				{ "ALL:#query#", "+Test test2", "ALL:(+Test test2)" }, //1
				{ "ALL:#query#", "Test*", "ALL:(Test*)" }, //2
				{ "ALL:#query#", "Test* test2", "ALL:(Test* test2)" }, //3
				{ "ALL:#+query#", "-Test", "ALL:(-Test)" }, //4
				{ "ALL:#+query#", "-Test test2", "ALL:(-Test +test2)" }, //5
				{ "+ALL:#query#", "-Test", "+ALL:(-Test)" }, //6
				{ "+ALL:#query#", "-Test test2", "+ALL:(-Test test2)" }, //7
				{ "ALL:#+query*#", "-Test", "ALL:(-Test*)" }, //8
				{ "ALL:#+query*#", "-Test test2", "ALL:(-Test* +test2*)" }, //9
		};
		testStringFixedQuery(testQueries[2]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringFixedQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:fixedValue", "Test", "ALL:fixedValue" },
				{ "ALL:fixedValue", "Test test2", "ALL:fixedValue" },
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringEmptyQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#+query*# +security:fixedValue", "Test", "ALL:(+Test*) +security:fixedValue" },
				{ "ALL:#+query*# +security:fixedValue", "*", "* +security:fixedValue" },
				{ "ALL:#+query*# +security:fixedValue", "*:*", "*:* +security:fixedValue" },
		};
		testStringFixedQuery(testQueries[2]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringSpecialCharQuery() {
		//ElasticSearch reserved characters are: + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#+query*#", "-Test", "ALL:(-Test*)" },
				{ "ALL:#+query*#", "Test-", "ALL:(+Test-)" },
				{ "ALL:#+query*#", "-Test-", "ALL:(-Test-)" },
				{ "ALL:#+query*#", "+Test+", "ALL:(+Test+)" },
				{ "ALL:#+query*#", "=Test=", "ALL:(=Test=)" },
				//{ "ALL:#+query*#", "&Test&", "ALL:(&Test&)" },
				//{ "ALL:#+query*#", "|Test|", "ALL:(|Test|)" },
				{ "ALL:#+query*#", ">Test>", "ALL:(>Test>)" },
				{ "ALL:#+query*#", "<Test<", "ALL:(<Test<)" },
				{ "ALL:#+query*#", "!Test!", "ALL:(!Test!)" },
				{ "ALL:#+query*#", "^Test^", "ALL:(^Test^)" },
				{ "ALL:#+query*#", "\"Test\"", "ALL:(\"Test\")" },
				{ "ALL:#+query*#", "~Test~", "ALL:(~Test~)" },
				{ "ALL:#+query*#", "*Test*", "ALL:(*Test*)" },
				{ "ALL:#+query*#", "?Test?", "ALL:(?Test?)" },
				//{ "ALL:#+query*#", ":Test:", "ALL:(:Test:)" },
				//{ "ALL:#+query*#", "\\Test\\", "ALL:(\\Test\\)" },
				//{ "ALL:#+query*#", "/Test/", "ALL:(/Test/)" },
				{ "ALL:#+query*#", ",Test,", "ALL:(,+Test*,)" },
				{ "ALL:#+query*#", ";Test;", "ALL:(;+Test*;)" },

		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringHackQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query# +security:fixedValue", "Test OR 1=1", "ALL:(Test OR 1=1) +security:fixedValue" },
				{ "ALL:#query# +security:fixedValue", "Test) OR (1=1", "ALL:(Test) OR (1=1) +security:fixedValue" },
		};
		testStringFixedQuery(testQueries);
	}

	private void testStringFixedQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			final ListFilterBuilder<String> listFilterBuilder = new DefaultListFilterBuilder<String>()
					.withBuildQuery(testParam[0])
					.withCriteria(testParam[1]);
			final String result = listFilterBuilder.build().getFilterValue();
			Assert.assertEquals("Built query #" + i + " incorrect", testParam[2], result);
			i++;
		}
	}
}
