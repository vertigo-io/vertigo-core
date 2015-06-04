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

	private static final String[][] TEST_STRING_QUERY = new String[][] {
			//QueryPattern, UserQuery, EspectedResult
			{ "ALL:#query#", "Test", "ALL:(Test)" },
			{ "ALL:#query#", "Test test2", "ALL:(Test test2)" },
			{ "ALL:#query*#", "Test", "ALL:(Test*)" },
			{ "ALL:#query*#", "Test test2", "ALL:(Test* test2*)" },
			{ "ALL:#+query#", "Test", "ALL:(+Test)" },
			{ "ALL:#+query#", "Test test2", "ALL:(+Test +test2)" },
			{ "+ALL:#query#", "Test", "+ALL:(Test)" },
			{ "+ALL:#query#", "Test test2", "+ALL:(Test test2)" },
	};

	private static final String[][] TEST_STRING_ADVANCED_QUERY = new String[][] {
			//QueryPattern, UserQuery, EspectedResult
			{ "ALL:#query#", "Test or test2", "ALL:(Test or test2)" },
			{ "ALL:#query#", "Test and test2", "ALL:(Test and test2)" },
			{ "ALL:#query#", "Test OR test2", "ALL:(Test OR test2)" },
			{ "ALL:#query#", "Test AND test2", "ALL:(Test AND test2)" },
			{ "ALL:#query#", "Test AND (test2 OR test3)", "ALL:(Test AND (test2 OR test3))" },
			{ "ALL:#query*#", "Test AND test2", "ALL:(Test* AND test2*)" },
			{ "ALL:#query*#", "Test AND (test2 OR test3)", "ALL:(Test* AND (test2* OR test3*))" },
			{ "ALL:#+query*#", "Test AND (test2 OR test3)", "ALL:(+Test* AND (+test2* OR +test3*))" },
			{ "+ALL:#query#", "Test or test2", "+ALL:(Test or test2)" },
	};

	private static final String[][] TEST_STRING_OVERRIDED_FIELD_QUERY = new String[][] {
			//QueryPattern, UserQuery, EspectedResult
			{ "ALL:#query#", "OTHER:Test", "OTHER:(Test)" },
			{ "ALL:#+query*#", "OTHER:Test", "OTHER:(Test)" },
			{ "ALL:#query#", "OTHER:Test test2", "OTHER:(Test) ALL:(test2)" },
			{ "ALL:#query#", "Test OTHER:test2", "ALL:(Test) OTHER:(test2)" },
			{ "ALL:#query#", "OTHER:Test test2 test3", "OTHER:(Test) ALL:(test2 test3)" },
			{ "ALL:#query#", "Test OTHER:test2 test3", "ALL:(Test) OTHER:(test2) ALL:(test3)" },
			{ "ALL:#query#", "Test test2 OTHER:test3", "ALL:(Test test2) OTHER:(test3)" },
			{ "ALL:#+query*#", "Test test2 OTHER:test3", "ALL:(+Test* +test2*) OTHER:(test3)" },
			{ "+ALL:#query#", "Test test2 OTHER:test3", "+ALL:(Test test2) OTHER:(test3)" },
			{ "ALL:#+query*#", "Test OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)" },
			{ "+ALL:#query#", "Test OTHER:(test2 test3)", "+ALL:(Test) OTHER:(test2 test3)" },
			{ "ALL:#query#", "Test +OTHER:(test2 test3)", "ALL:(Test) +OTHER:(test2 test3)" },
	};

	private static final String[][] TEST_STRING_OVERRIDED_MODIFIER_QUERY = new String[][] {
			//QueryPattern, UserQuery, EspectedResult
			{ "ALL:#query#", "+Test", "ALL:(+Test)" },
			{ "ALL:#query#", "+Test test2", "ALL:(+Test test2)" },
			{ "ALL:#query#", "Test*", "ALL:(Test*)" },
			{ "ALL:#query#", "Test* test2", "ALL:(Test* test2)" },
			{ "ALL:#+query#", "-Test", "ALL:(-Test)" },
			{ "ALL:#+query#", "-Test test2", "ALL:(-Test +test2)" },
			{ "+ALL:#query#", "-Test", "+ALL:(-Test)" },
			{ "+ALL:#query#", "-Test test2", "+ALL:(-Test test2)" },
			{ "ALL:#+query*#", "-Test", "ALL:(-Test*)" },
			{ "ALL:#+query*#", "-Test test2", "ALL:(-Test* +test2*)" },
	};

	private static final String[][] TEST_STRING_FIXED_QUERY = new String[][] {
			//QueryPattern, UserQuery, EspectedResult
			{ "ALL:fixedValue", "Test", "ALL:fixedValue" },
			{ "ALL:fixedValue", "Test test2", "ALL:fixedValue" },
	};

	@Test
	public void testStringQuery() {
		testStringFixedQuery(TEST_STRING_QUERY);
	}

	@Test
	public void testStringAdvancedQuery() {
		testStringFixedQuery(TEST_STRING_ADVANCED_QUERY);
	}

	@Test
	public void testStringOverridedFieldQuery() {
		testStringFixedQuery(TEST_STRING_OVERRIDED_FIELD_QUERY);
	}

	@Test
	public void testStringOverridedModifierQuery() {
		testStringFixedQuery(TEST_STRING_OVERRIDED_MODIFIER_QUERY);
	}

	@Test
	public void testStringFixedQuery() {
		testStringFixedQuery(TEST_STRING_FIXED_QUERY);
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
