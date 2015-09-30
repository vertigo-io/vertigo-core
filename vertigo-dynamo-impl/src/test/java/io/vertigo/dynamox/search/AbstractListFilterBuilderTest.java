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
import io.vertigo.util.DateUtil;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 */
public abstract class AbstractListFilterBuilderTest {

	@Test
	public void testStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult, OtherAcceptedResult ...
				{ "ALL:#query#", "Test", "ALL:(Test)", "ALL:Test" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query*#", "Test", "ALL:(Test*)", "ALL:Test*" }, //2
				{ "ALL:#query*#", "Test test2", "ALL:(Test* test2*)" }, //3
				{ "ALL:#+query#", "Test", "ALL:(+Test)", "ALL:+Test" }, //4
				{ "ALL:#+query#", "Test test2", "ALL:(+Test +test2)" }, //5
				{ "+ALL:#query#", "Test", "+ALL:(Test)", "+ALL:Test" }, //6
				{ "+ALL:#query#", "Test test2", "+ALL:(Test test2)" }, //7
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringGlobalModifierQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "Test", "ALL:(Test)", "ALL:Test" }, //0
				{ "ALL:#query#", "Test test2", "ALL:(Test test2)" }, //1
				{ "ALL:#query#*", "Test", "ALL:(Test)*", "ALL:(Test*)" }, //2
				{ "ALL:#query#*", "Test test2", "ALL:(Test test2)*", "ALL:((Test test2)*)" }, //3
				{ "ALL:+#query#", "Test", "ALL:+(Test)", "ALL:(+Test)" }, //4
				{ "ALL:+#query#", "Test test2", "ALL:+(Test test2)", "ALL:(+(Test test2))" }, //5
				{ "-ALL:+#query#*", "Test", "-ALL:+(Test)*", "-ALL:(+Test*)" }, //6
				{ "-ALL:+#query#*", "Test test2", "-ALL:(+(Test test2)*)" }, //7
				{ "-ALL:+#query#*", "Test AND (test2 OR test3)", "-ALL:(+(Test AND (test2 OR test3))*)" }, //8
		};
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
				{ "ALL:(#query# #query*# #Query~2#)", "Test test2", "ALL:((Test test2) Test* test2* (Test~2 test2~2))", "ALL:((Test test2) (Test* test2*) (Test~2 test2~2))" }, //16
				{ "ALL:(#query#^4 #query*#^2 #Query~2#)", "Test test2", "ALL:((Test test2)^4 (Test* test2*)^2 (Test~2 test2~2))" }, //17
		};
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testNullableStringQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "", "ALL:*" }, //0
				{ "+YEAR:[2000 to #query#!(*)]", "", "+YEAR:[2000 to *]" }, //1
		};
		testStringFixedQuery(testQueries[1]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringOverridedFieldQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query#", "OTHER:Test", "OTHER:(Test)", "OTHER:Test" }, //0
				{ "ALL:#+query*#", "OTHER:Test", "OTHER:(Test)", "OTHER:Test" }, //1
				{ "ALL:#query#", "OTHER:Test test2", "OTHER:(Test) ALL:(test2)", "OTHER:Test ALL:test2" }, //2
				{ "ALL:#query#", "Test OTHER:test2", "ALL:(Test) OTHER:(test2)", "ALL:Test OTHER:test2" }, //3
				{ "ALL:#query#", "OTHER:Test test2 test3", "OTHER:(Test) ALL:(test2 test3)", "OTHER:Test ALL:(test2 test3)" }, //4
				{ "ALL:#query#", "Test OTHER:test2 test3", "ALL:(Test) OTHER:(test2), ALL:(test3)", "ALL:Test OTHER:test2 ALL:test3" }, //5
				{ "ALL:#query#", "Test test2 OTHER:test3", "ALL:(Test test2) OTHER:(test3)", "ALL:(Test test2) OTHER:test3" }, //6
				{ "ALL:#+query*#", "Test test2 OTHER:test3", "ALL:(+Test* +test2*) OTHER:(test3)", "ALL:(+Test* +test2*) OTHER:test3" }, //7
				{ "+ALL:#query#", "Test test2 OTHER:test3", "+ALL:(Test test2) OTHER:(test3)", "+ALL:(Test test2) OTHER:test3" }, //8
				{ "ALL:#+query*#", "Test OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)", "ALL:(+Test*) OTHER:(test2 test3)" }, //9
				{ "+ALL:#query#", "Test OTHER:(test2 test3)", "+ALL:(Test) OTHER:(test2 test3)", "+ALL:Test OTHER:(test2 test3)" }, //10
				{ "ALL:#query#", "Test -OTHER:(test2 test3)", "ALL:(Test) -OTHER:(test2 test3)", "ALL:Test -OTHER:(test2 test3)" }, //11
				{ "ALL:#+query*#", "Test test2~", "ALL:(+Test* +test2~)" }, //12
				{ "ALL:#query#", "Test -OTHER:(test2 test3) Test4", "ALL:(Test) -OTHER:(test2 test3) ALL:(Test4)", "ALL:Test -OTHER:(test2 test3) ALL:Test4" }, //13
		};
		testStringFixedQuery(testQueries[6]);
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
				{ "ALL:#+query*# +security:fixedValue", "Test", "ALL:(+Test*) +security:fixedValue" }, //0
				{ "ALL:#+query*# +security:fixedValue", "*", "ALL:* +security:fixedValue" }, //1
				{ "ALL:#+query*# +security:fixedValue", "*:*", "*:* +security:fixedValue" }, //2
				{ "ALL:#+query*# +security:fixedValue", " ", "ALL:* +security:fixedValue" }, //3
				{ "ALL:#+query*# +security:fixedValue", "", "ALL:* +security:fixedValue" }, //4
				{ "ALL:#+query*# +security:fixedValue", "YEAR:*", "YEAR:* +security:fixedValue" }, //5
		};
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
				{ "ALL:#+query*#", "\"Test\"", "ALL:(\"Test\")", "ALL:\"Test\"" },
				{ "ALL:#+query*#", "~Test~", "ALL:(~Test~)" },
				{ "ALL:#+query*#", "*Test*", "ALL:(*Test*)" },
				{ "ALL:#+query*#", "?Test?", "ALL:(?Test?)" },
				//{ "ALL:#+query*#", ":Test:", "ALL:(:Test:)" },
				//{ "ALL:#+query*#", "\\Test\\", "ALL:(\\Test\\)" },
				//{ "ALL:#+query*#", "/Test/", "ALL:(/Test/)" },
				{ "ALL:#+query*#", ",Test,", "ALL:(,+Test*,)" },
				{ "ALL:#+query*#", ";Test;", "ALL:(;+Test*;)" },
				{ "ALL:#+query*#", "(Test)", "ALL:(+Test*)" },
				{ "ALL:#+query*#", "[Test]", "ALL:[Test]" },

		};
		testStringFixedQuery(testQueries[13]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testStringHackQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#query# +security:fixedValue", "Test OR 1=1", "ALL:(Test OR 1=1) +security:fixedValue" },
				{ "ALL:#query# +security:\"fixedValue\"", "Test OR 1=1", "ALL:(Test OR 1=1) +security:\"fixedValue\"" },
				{ "ALL:#query# +security:fixedValue", "Test) OR (1=1", "ALL:(Test) OR (1=1) +security:fixedValue" }, //don't affect security
				{ "ALL:#query# +security:fixedValue", "*) OR ", "ALL:(*) OR) +security:fixedValue" },
		};
		testStringFixedQuery(testQueries[3]);
		testStringFixedQuery(testQueries);
	}

	@Test
	public void testBeanQuery() {
		final Date dateTest1 = DateUtil.parse("230715 123000 -00", "ddMMyy HHmmss X");
		final Date dateTest2 = DateUtil.parse("230715 164500 -00", "ddMMyy HHmmss X");
		final TestBean testBean = new TestBean("Test", "Test test2", dateTest1, dateTest2, 5, 10);
		final Object[][] testQueries = new Object[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "ALL:#str1#", testBean, "ALL:(Test)", "ALL:Test" }, //0
				{ "ALL:#str2#", testBean, "ALL:(Test test2)" }, //1
				{ "ALL:#date1#", testBean, "ALL:\"2015-07-23T12:30:00.000Z\"", "ALL:(\"2015-07-23T12:30:00.000Z\")" }, //2
				{ "ALL:#date2#", testBean, "ALL:\"2015-07-23T16:45:00.000Z\"", "ALL:(\"2015-07-23T16:45:00.000Z\")" }, //3
				{ "ALL:#int1#", testBean, "ALL:5" }, //4
				{ "ALL:#int2#", testBean, "ALL:10" }, //5
				{ "ALL:[#int1# to #int2#] ", testBean, "ALL:[5 to 10]" }, //6
				{ "ALL:[#int1# TO #int2#] ", testBean, "ALL:[5 to 10]" }, //7
				{ "ALL:[#date1# to #date2#] ", testBean, "ALL:[\"2015-07-23T12:30:00.000Z\" to \"2015-07-23T16:45:00.000Z\"]" }, //8
				{ "ALL:[#int1# to #null#] ", testBean, "ALL:[5 to *]" }, //9
				{ "ALL:[#int1# to #null#!(*)] ", testBean, "ALL:[5 to *]" }, //10
				{ "ALL:[#null#!(*) to #int2#] ", testBean, "ALL:[* to 10]" }, //11
				{ "ALL:[#null# to #null#] ", testBean, "" }, //12
				{ "ALL:[ #null# to #null# ] ", testBean, "" }, //13
				{ "ALL:[#date1# to #null#!(*)] ", testBean, "ALL:[\"2015-07-23T12:30:00.000Z\" to *]" }, //14
				{ "ALL:[#null#!(*) to #null#!(*)] ", testBean, "" }, //12

		};
		testObjectFixedQuery(testQueries[12]);
		testObjectFixedQuery(testQueries);
	}

	@Test
	public void testMultiQuery() {
		final TestBean testBeanNull = new TestBean(null, "Test test2", null, null, null, null);
		final TestBean testBeanEmpty = new TestBean("", "Test test2", null, null, null, null);
		final TestBean testBeanOne = new TestBean("12", "Test test2", null, null, null, null);
		final TestBean testBeanMultiple = new TestBean("12 13", "Test test2", null, null, null, null);
		final TestBean testBeanMultipleCode = new TestBean("CODE_1 CODE_3", "Test test2", null, null, null, null);
		final Object[][] testQueries = new Object[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanNull, " +ALL:(Test test2)" }, //0
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanEmpty, "+PRO_ID:* +ALL:(Test test2)" }, //1
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanOne, "+PRO_ID:(12) +ALL:(Test test2)", "+PRO_ID:12 +ALL:(Test test2)" }, //2
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanMultiple, "+PRO_ID:(12 13) +ALL:(Test test2)" }, //3
				{ "+PRO_ID:#+str1# +ALL:#str2#", testBeanMultiple, "+PRO_ID:(+12 +13) +ALL:(Test test2)" }, //4
				{ "+PRO_ID:#str1# +ALL:#str2#", testBeanMultipleCode, "+PRO_ID:(CODE_1 CODE_3) +ALL:(Test test2)" }, //5
				{ "+PRO_ID:#+str1# +ALL:#str2#", testBeanMultipleCode, "+PRO_ID:(+CODE_1 +CODE_3) +ALL:(Test test2)" }, //6
				{ "+(PRO_ID:#str1#) +ALL:#str2#", testBeanNull, " +ALL:(Test test2)" }, //7

		};
		testObjectFixedQuery(testQueries[7]);
		testObjectFixedQuery(testQueries);
	}

	@Test
	public void testMultiFieldQuery() {
		final String[][] testQueries = new String[][] {
				//QueryPattern, UserQuery, EspectedResult
				{ "+FIELD_1:#query*#", "Test test2", "+FIELD_1:(Test* test2*)" }, //0
				{ "[FIELD_1,FIELD_2]:#query*#", "Test test2", "FIELD_1:(Test*) FIELD_2:(Test*) FIELD_1:(test2*) FIELD_2:(test2*)"
						, "((FIELD_1:(Test*) FIELD_2:(Test*)) (FIELD_1:(test2*) FIELD_2:(test2*)))" }, //1
				{ "+[FIELD_1,FIELD_2]:#query*#", "Test test2", "+(FIELD_1:(Test*) FIELD_2:(Test*) FIELD_1:(test2*) FIELD_2:(test2*))",
						"+((FIELD_1:(Test*) FIELD_2:(Test*)) (FIELD_1:(test2*) FIELD_2:(test2*)))" }, //2
				{ "+([FIELD_1,FIELD_2]:#query*#)", "Test test2", "+((FIELD_1:(Test*) FIELD_2:(Test*) FIELD_1:(test2*) FIELD_2:(test2*)))",
						"+(((FIELD_1:(Test*) FIELD_2:(Test*)) (FIELD_1:(test2*) FIELD_2:(test2*))))" }, //3
				{ "[FIELD_1,FIELD_2]:#+query*#", "Test test2", "+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*))",
						"(+(FIELD_1:(Test*) FIELD_2:(Test*)) +(FIELD_1:(test2*) FIELD_2:(test2*)))" }, //4
				//error { "[+FIELD_1,FIELD2]:#query*#", "Test test2", "+((FIELD_1:(Test*) FIELD_2:(Test*)) (FIELD_1:(test2*) FIELD_2:(test2*)))" }, //4
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "Test test2", "+(FIELD_1:(Test*) FIELD_2:(Test*)^2) +(FIELD_1:(test2*) FIELD_2:(test2*)^2)",
						"(+(FIELD_1:(Test*) FIELD_2:(Test*)^2) +(FIELD_1:(test2*) FIELD_2:(test2*)^2))" }, //5
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "Test ALL:test2", "+(FIELD_1:(Test*) FIELD_2:(Test*)^2) ALL:(test2)",
						"(+(FIELD_1:(Test*) FIELD_2:(Test*)^2)) ALL:test2" }, //6
				{ "[FIELD_1,FIELD_2^2]:#+query*#", "ALL:test2 Test", "ALL:(test2) +(FIELD_1:(Test*) FIELD_2:(Test*)^2)",
						"ALL:test2 (+(FIELD_1:(Test*) FIELD_2:(Test*)^2))" }, //7

				{ "+[FIELD_1,FIELD_2]:(#query#^4 #query*#^2 #query~2#)", "Test test2",
						"+(((FIELD_1:Test FIELD_2:Test) (FIELD_1:test2 FIELD_2:test2))^4 ((FIELD_1:(Test*) FIELD_2:(Test*)) (FIELD_1:(test2*) FIELD_2:(test2*)))^2 ((FIELD_1:(Test~2) FIELD_2:(Test~2)) (FIELD_1:(test2~2) FIELD_2:(test2~2))))" }, //8
		};
		testStringFixedQuery(testQueries);
	}

	abstract <O> ListFilterBuilder<O> createListFilterBuilder(Class<O> criteriaType);

	abstract int getPreferedResult();

	private void testStringFixedQuery(final String[]... testData) {
		int i = 0;
		for (final String[] testParam : testData) {
			final ListFilterBuilder<String> listFilterBuilder = createListFilterBuilder(String.class)
					.withBuildQuery(testParam[0])
					.withCriteria(testParam[1]);
			final String result = listFilterBuilder.build().getFilterValue();
			boolean found = false;
			for (int j = 2; j < testParam.length; j++) {
				if (testParam[j].equalsIgnoreCase(result)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Assert.assertEquals("Built query #" + i + " incorrect", testParam[Math.min(getPreferedResult(), testParam.length - 1)], result);
			}
			i++;
		}
	}

	private void testObjectFixedQuery(final Object[]... testData) {
		int i = 0;
		for (final Object[] testParam : testData) {
			final ListFilterBuilder<Object> listFilterBuilder = createListFilterBuilder(Object.class)
					.withBuildQuery((String) testParam[0])
					.withCriteria(testParam[1]);
			final String result = listFilterBuilder.build().getFilterValue();
			boolean found = false;
			for (int j = 2; j < testParam.length; j++) {
				if (((String) testParam[j]).trim().equalsIgnoreCase(result.trim())) {
					found = true;
					break;
				}
			}
			if (!found) {
				Assert.assertEquals("Built query #" + i + " incorrect", testParam[Math.min(getPreferedResult(), testParam.length - 1)], result);
			}
			i++;
		}
	}

	public static class TestBean {

		private final String str1;
		private final String str2;
		private final Date date1;
		private final Date date2;
		private final Integer int1;
		private final Integer int2;

		TestBean(final String str1, final String str2,
				final Date date1, final Date date2,
				final Integer int1, final Integer int2) {
			this.str1 = str1;
			this.str2 = str2;
			this.date1 = date1;
			this.date2 = date2;
			this.int1 = int1;
			this.int2 = int2;
		}

		public String getStr1() {
			return str1;
		}

		public String getStr2() {
			return str2;
		}

		public Date getDate1() {
			return date1;
		}

		public Date getDate2() {
			return date2;
		}

		public Integer getInt1() {
			return int1;
		}

		public Integer getInt2() {
			return int2;
		}

		public Object getNull() {
			return null;
		}

	}

}
