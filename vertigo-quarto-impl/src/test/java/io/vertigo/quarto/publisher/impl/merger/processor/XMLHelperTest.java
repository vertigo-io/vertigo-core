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
package io.vertigo.quarto.publisher.impl.merger.processor;

import java.util.Deque;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Classe utilitaire pour une arborescence XML.
 *
 * @author npiedeloup
 */
public final class XMLHelperTest {
	/** Logger. */
	private final Logger LOG = Logger.getLogger(getClass());

	@Test
	public void testMinimalXML() {
		final String[] xmlTest = { "<a b=\"test\"></a>", "<a></a></a>", "<a b=\"test\"></a><a c=\"test2\">", "</b></r><a b=\"test\"></a>", "</r></c></l><l b=\"test\"><c d=\"test3\"></c><c c=\"test2\">", "</b></c></l><l><c><r>", "</b></c></l><a><l><c><r>", "</sc></s></s><s><s>a</s></s><s><s><sc>", };
		final String[] xmlTestResult = { "", "</a>", "<a c=\"test2\">", "</b></r>", "</r></c></l><l b=\"test\"><c c=\"test2\">", "</b></c></l><l><c><r>", "</b></c></l><a><l><c><r>", "</sc></s></s><s><s><sc>", "" };

		for (int i = 0; i < xmlTest.length; i++) {
			//log.trace("Text:" + xmlTest[i]);
			//log.trace(", \"" + getMinimalXML(xmlTest[i]) + "\"");
			Assert.assertEquals(xmlTestResult[i], getMinimalXML(xmlTest[i]));
		}
	}

	@Test
	public void testXMLBalancer() {
		final String[] xmlTest2 = { "body", "body<r>", "<r>body", "</b></c></l><a><l><c><r>", "<a b=\"test\">&lt;#loop #&gt;<b></b>&lt;#endloop#&gt;</a>", "<a b=\"test\"></a>&lt;#loop #&gt;<a c=\"test2\">&lt;#endloop#&gt;</a>", "<r><b>&lt;#loop #&gt;</b></r><a b=\"test\">&lt;#endloop#&gt;</a>", "<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2&lt;#loop #&gt;</r></c></l><l row=\"2\"><c c=\"1\">data1</c><c c=\"2\">data2&lt;#endloop#&gt;</c></l></t>",
				"<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2&lt;#loop #&gt;</r></c></l><l row=\"2\"><c c=\"1\">data1</c><c c=\"2\"><g>data2&lt;#endloop#&gt;</g></c></l></t>", "<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2</r></c></l><l row=\"2\"><c c=\"1\">&lt;#loop #&gt;data1</c><c c=\"2\">data2&lt;#endloop#&gt;</c></l></t>",
				"<text:script script:language=\"KScript\">&lt;#if BOOLEAN_2#&gt;</text:script></text:span></text:span><text:span text:style-name=\"texte\"><text:span text:style-name=\"T3\">boolean 2</text:span></text:span><text:span text:style-name=\"texte\"><text:span text:style-name=\"T1\"><text:script script:language=\"KScript\">&lt;#endif#&gt;</text:script>", };
		final GrammarXMLBalancerProcessor prepross = new GrammarXMLBalancerProcessor();
		for (final String xml : xmlTest2) {
			LOG.trace("Text:" + xml);

			final int index = ProcessorXMLUtil.getFirstBodyIndex(xml);
			final int lindex = ProcessorXMLUtil.getLastBodyEndIndex(xml);
			LOG.trace("  getFirstBodyIndex:" + index + "   " + (index != -1 ? xml.substring(index) : ""));
			LOG.trace("  getLastBodyEndIndex:" + lindex + "   " + (lindex != -1 ? xml.substring(0, lindex) : ""));
			LOG.trace("  prepross:" + prepross.execute(xml, null));
		}
	}

	@Test
	public void testXMLBalancerEnclosed() {
		final String[] xmlTest2 = {
				"body",
				"body<r>",
				"<r>body",
				"</b></c></l><a><l><c><r>",
				"<a b=\"test\">&lt;#loop #&gt;<b></b>&lt;#endloop#&gt;</a>",
				"<a b=\"test\"></a>&lt;#loop #&gt;<a c=\"test2\">&lt;#endloop#&gt;</a>",
				"<r><b>&lt;#loop #&gt;</b></r><a b=\"test\">&lt;#endloop#&gt;</a>",
				"<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2&lt;#loop #&gt;</r></c></l><l row=\"2\"><c c=\"1\">data1</c><c c=\"2\">data2&lt;#endloop#&gt;</c></l></t>",
				"<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2&lt;#loop #&gt;</r></c></l><l row=\"2\"><c c=\"1\">data1</c><c c=\"2\"><g>data2&lt;#endloop#&gt;</g></c></l></t>",
				"<t><l row=\"1\"><c c=\"1\">header1</c><c c=\"2\"><r>header2</r></c></l><l row=\"2\"><c c=\"1\">&lt;#loop #&gt;data1</c><c c=\"2\">data2&lt;#endloop#&gt;</c></l></t>",
				"<text:script script:language=\"KScript\">&lt;#if BOOLEAN_2#&gt;</text:script></text:span></text:span><text:span text:style-name=\"texte\"><text:script script:language=\"KScript\">&lt;#if BOOLEAN_3#&gt;</text:script><text:span text:style-name=\"T3\">boolean 2</text:span><text:script script:language=\"KScript\">&lt;#endif#&gt;</text:script></text:span><text:span text:style-name=\"texte\"><text:span text:style-name=\"T1\"><text:script script:language=\"KScript\">&lt;#endif#&gt;</text:script>",
				"<text:script script:language=\"KScript\">&lt;#var toto : BOOLEAN_2#&gt;</text:script></text:span></text:span><text:span text:style-name=\"texte\"><text:script script:language=\"KScript\">&lt;#var titi : BOOLEAN_3#&gt;</text:script><text:span text:style-name=\"T3\">boolean 2</text:span><text:script script:language=\"KScript\">&lt;#endvar#&gt;</text:script></text:span><text:span text:style-name=\"texte\"><text:span text:style-name=\"T1\"><text:script script:language=\"KScript\">&lt;#endvar#&gt;</text:script>", };
		final GrammarXMLBalancerProcessor prepross = new GrammarXMLBalancerProcessor();
		for (final String xml : xmlTest2) {
			LOG.trace("Text:" + xml);

			final int index = ProcessorXMLUtil.getFirstBodyIndex(xml);
			final int lindex = ProcessorXMLUtil.getLastBodyEndIndex(xml);
			LOG.trace("  getFirstBodyIndex:" + index + "   " + (index != -1 ? xml.substring(index) : ""));
			LOG.trace("  getLastBodyEndIndex:" + lindex + "   " + (lindex != -1 ? xml.substring(0, lindex) : ""));
			LOG.trace("  prepross:" + prepross.execute(xml, null));
		}
	}

	@Test
	public void testXMLBalancerBig() {
		final String[] xmlTest2 = { "<script>&lt;#var address2 = ADRESSE_RATACHEMENT#&gt;</script>" + "<span><span>" + "<script>&lt;#endvar#&gt;</script>" + "</span></span>" };
		final GrammarXMLBalancerProcessor prepross = new GrammarXMLBalancerProcessor();
		for (final String xml : xmlTest2) {
			LOG.trace("Text:" + getMinimalXML(xml));

			final int index = ProcessorXMLUtil.getFirstBodyIndex(xml);
			final int lindex = ProcessorXMLUtil.getLastBodyEndIndex(xml);
			LOG.trace("  getFirstBodyIndex:" + index + "   " + (index != -1 ? xml.substring(index) : ""));
			LOG.trace("  getLastBodyEndIndex:" + lindex + "   " + (lindex != -1 ? xml.substring(0, lindex) : ""));
			LOG.trace("  prepross:" + prepross.execute(xml, null));
		}
	}

	private static String getMinimalXML(final String xmlContent) {
		final Deque<TagXML> pileTag = ProcessorXMLUtil.extractUnbalancedTag(xmlContent.toCharArray());

		final StringBuilder contentClean = new StringBuilder();
		for (final TagXML tag : pileTag) {
			contentClean.append(tag.getFullTag());
		}
		return contentClean.toString();
	}
}
