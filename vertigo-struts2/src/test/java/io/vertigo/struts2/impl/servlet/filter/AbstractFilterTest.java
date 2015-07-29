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
package io.vertigo.struts2.impl.servlet.filter;

import io.vertigo.lang.Option;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author npiedeloup
 */
public class AbstractFilterTest {

	@Test
	public final void testExactMatch() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myExactMatchTest.html");
		Assert.assertTrue(pattern.isDefined());

		final String uri = "http://localhost:8080/testFilter/myExactMatchTest.html";
		final boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);
	}

	@Test
	public final void testMiddleStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/my*MatchTest.html");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myNotSeparatedStarMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/mySeparated/StarMatchTest.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertFalse(uri + " match " + pattern.get(), result);
	}

	@Test
	public final void testTwoMiddleStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/first*Match1Test.html;/other*Match2Test.html");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/firstNotSeparatedStarMatch1Test.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/otherNotSeparatedStarMatch2Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/firstNotSeparatedStarMatch2Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertFalse(uri + " match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/otherNotSeparatedStarMatch1Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertFalse(uri + " match " + pattern.get(), result);
	}

	@Test
	public final void testEndStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myEndMatchTest.*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myEndMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.xhtml";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.do";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.middle/orOtherpath";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);
	}

	@Test
	public final void testTwoEndStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myEndMatchTest.*;/myPathMatchTest/*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myEndMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.do";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/andTheEnd.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/otherPath/andAtLast.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myPathMatchTestCompleted/andEndIt.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertFalse(uri + " match " + pattern.get(), result);

	}

	@Test
	public final void testPathMatch() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myPathMatchTest*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myPathMatchTest/andTheEnd.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/otherPath/andAtLast.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);

		uri = "http://localhost:8080/testFilter/myPathMatchTestCompleted/andEndIt.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertTrue(uri + " doesn't match " + pattern.get(), result);
	}
}
