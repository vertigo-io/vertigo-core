package io.vertigo.struts2.impl.servlet.filter;

import io.vertigo.core.lang.Option;

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
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);
	}

	@Test
	public final void testMiddleStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/my*MatchTest.html");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myNotSeparatedStarMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/mySeparated/StarMatchTest.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " match " + pattern.get(), false, result);
	}

	@Test
	public final void testTwoMiddleStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/first*Match1Test.html;/other*Match2Test.html");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/firstNotSeparatedStarMatch1Test.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/otherNotSeparatedStarMatch2Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/firstNotSeparatedStarMatch2Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " match " + pattern.get(), false, result);

		uri = "http://localhost:8080/testFilter/otherNotSeparatedStarMatch1Test.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " match " + pattern.get(), false, result);
	}

	@Test
	public final void testEndStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myEndMatchTest.*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myEndMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.xhtml";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.do";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.middle/orOtherpath";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);
	}

	@Test
	public final void testTwoEndStar() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myEndMatchTest.*;/myPathMatchTest/*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myEndMatchTest.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myEndMatchTest.do";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/andTheEnd.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/otherPath/andAtLast.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myPathMatchTestCompleted/andEndIt.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " match " + pattern.get(), false, result);

	}

	@Test
	public final void testPathMatch() {
		final Option<Pattern> pattern = AbstractFilter.parsePattern("/myPathMatchTest*");
		Assert.assertTrue(pattern.isDefined());

		String uri = "http://localhost:8080/testFilter/myPathMatchTest/andTheEnd.html";
		boolean result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myPathMatchTest/otherPath/andAtLast.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);

		uri = "http://localhost:8080/testFilter/myPathMatchTestCompleted/andEndIt.html";
		result = AbstractFilter.isUrlMatch("testFilter", uri, pattern.get());
		Assert.assertEquals(uri + " doesn't match " + pattern.get(), true, result);
	}
}
