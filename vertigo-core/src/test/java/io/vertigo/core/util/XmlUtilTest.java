/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.lang.WrappedException;

/**
 * Test de l'utilitaire de manipulation et de sécurisation XML (XXE).
 *
 * @author npiedeloup
 */
public final class XmlUtilTest {
	private static final String XXE_SECRET_MARKER = "VERTIGO_XXE_SECRET_MARKER";

	//----- removeDoctype

	@Test
	public void removeDoctypeWithoutDoctype() {
		final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root attr=\"v\">text</root>";
		assertEquals(xml, XmlUtil.removeDoctype(xml));
	}

	@Test
	public void removeDoctypeOnEmptyString() {
		assertEquals("", XmlUtil.removeDoctype(""));
	}

	@Test
	public void removeDoctypeSimple() {
		assertEquals("<root/>", XmlUtil.removeDoctype("<!DOCTYPE root><root/>"));
		assertEquals("<?xml version=\"1.0\"?><root/>", XmlUtil.removeDoctype("<?xml version=\"1.0\"?><!DOCTYPE root><root/>"));
	}

	@Test
	public void removeDoctypeXhtml() {
		assertEquals("<html><body/></html>", XmlUtil.removeDoctype("<!DOCTYPE html><html><body/></html>"));
	}

	@Test
	public void removeDoctypePublic() {
		assertEquals("<note/>", XmlUtil.removeDoctype("<!DOCTYPE note PUBLIC \"-//W3C//DTD Note 1.0//EN\" \"note.dtd\"><note/>"));
	}

	@Test
	public void removeDoctypeSystem() {
		assertEquals("<feed/>", XmlUtil.removeDoctype("<!DOCTYPE feed SYSTEM \"feed.dtd\"><feed/>"));
	}

	@Test
	public void removeDoctypeCaseInsensitive() {
		assertEquals("<r/>", XmlUtil.removeDoctype("<!doctype r><r/>"));
		assertEquals("<r/>", XmlUtil.removeDoctype("<!Doctype r SYSTEM \"x.dtd\"><r/>"));
	}

	@Test
	public void removeDoctypeBomWhitespaceAndXmlDeclarationBefore() {
		assertEquals("\uFEFF  <?xml version=\"1.0\"?>\n<r/>", XmlUtil.removeDoctype("\uFEFF  <?xml version=\"1.0\"?>\n<!DOCTYPE r><r/>"));
	}

	@Test
	public void removeDoctypeInternalSubsetWithBracketsAndGreaterThanInQuotes() {
		assertEquals("<root>&g;</root>", XmlUtil.removeDoctype("<!DOCTYPE root [ <!ENTITY g \"a > b [c] d\"> ]><root>&g;</root>"));
	}

	@Test
	public void removeDoctypeInternalSubsetWithBracketSequenceInQuotes() {
		assertEquals("<r/>", XmlUtil.removeDoctype("<!DOCTYPE r [ <!NOTATION n \"x]>y\"> <!ENTITY e 'a[b]c'> ]><r/>"));
	}

	@Test
	public void removeDoctypeNotAtBeginning() {
		final String xml = "<r><!DOCTYPE x></r>";
		assertEquals(xml, XmlUtil.removeDoctype(xml));
	}

	@Test
	public void removeDoctypeAfterProcessingInstruction() {
		final String xml = "<?note pi?><!DOCTYPE r><r/>";
		assertEquals(xml, XmlUtil.removeDoctype(xml));
	}

	@Test
	public void removeDoctypeUnclosed() {
		final String xml1 = "<!DOCTYPE r <r/>";
		assertEquals(xml1, XmlUtil.removeDoctype(xml1));
		final String xml2 = "<!DOCTYPE r [ <!ENTITY e \"x\"> <r/>";
		assertEquals(xml2, XmlUtil.removeDoctype(xml2));
	}

	@Test
	public void removeDoctypeNull() {
		Assertions.assertThrows(NullPointerException.class, () -> XmlUtil.removeDoctype(null));
	}

	//----- parseXmlSecure

	@Test
	public void parseXmlSecureWithDoctype() {
		final Document doc = XmlUtil.parseXmlSecure("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html><html><body>hello</body></html>");
		assertNull(doc.getDoctype());
		assertEquals("html", doc.getDocumentElement().getTagName());
		assertEquals("hello", doc.getElementsByTagName("body").item(0).getTextContent());
	}

	@Test
	public void parseXmlSecureXxeEntityContentNotLeaked(@TempDir final Path tempDir) throws Exception {
		final File secret = tempDir.resolve("secret.txt").toFile();
		Files.writeString(secret.toPath(), XXE_SECRET_MARKER);
		final String payload = "<!DOCTYPE r [<!ENTITY xxe SYSTEM \"file://" + secret.toURI().toASCIIString() + "\">]><r>&xxe;</r>";
		final Document doc = tryParseXmlSecure(payload);
		if (doc != null) {
			assertFalse(domToString(doc).contains(XXE_SECRET_MARKER));
		}
	}

	@Test
	@Timeout(60)
	public void parseXmlSecureBillionLaughsIsBounded() {
		final Document doc = tryParseXmlSecure(billionLaughs());
		if (doc != null) {
			assertTrue(totalTextLength(doc.getDocumentElement()) < 100_000, "entity expansion must be bounded");
		}
	}

	@Test
	public void parseXmlSecureWithoutDoctypeIsSameAsStrict() throws Exception {
		final String xml = "<?xml version=\"1.0\"?><a b=\"c\"><d>txt</d></a>";
		final Document viaSecure = XmlUtil.parseXmlSecure(xml);
		final DocumentBuilderFactory strictFactory = DocumentBuilderFactory.newInstance();
		XmlUtil.secureXmlXXEByOwasp(strictFactory);
		final Document viaStrict = strictFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		assertEquals(domToString(viaStrict), domToString(viaSecure));
	}

	@Test
	public void parseXmlSecureFromInputStream() {
		final byte[] bytes = "<?xml version=\"1.0\"?><!DOCTYPE html><html><title>é</title></html>".getBytes(StandardCharsets.UTF_8);
		final Document doc = XmlUtil.parseXmlSecure(new ByteArrayInputStream(bytes));
		assertNull(doc.getDoctype());
		assertEquals("é", doc.getElementsByTagName("title").item(0).getTextContent());
	}

	@Test
	public void parseXmlSecureMalformed() {
		Assertions.assertThrows(WrappedException.class, () -> XmlUtil.parseXmlSecure("<a></b>"));
	}

	@Test
	public void parseXmlSecureNull() {
		Assertions.assertThrows(NullPointerException.class, () -> XmlUtil.parseXmlSecure((String) null));
		Assertions.assertThrows(NullPointerException.class, () -> XmlUtil.parseXmlSecure((InputStream) null));
	}

	//----- secureXmlXXEByOwaspAllowDoctype

	@Test
	public void allowDoctypeParsesDocumentWithDoctype() throws Exception {
		final Document doc = parseWithAllowDoctype("<?xml version=\"1.0\"?><!DOCTYPE note [ ]><note>ok</note>");
		assertNotNull(doc.getDoctype());
		assertEquals("note", doc.getDocumentElement().getTagName());
		assertEquals("ok", doc.getDocumentElement().getTextContent());
	}

	@Test
	public void allowDoctypeDoesNotExpandExternalGeneralEntity(@TempDir final Path tempDir) throws Exception {
		final File secret = tempDir.resolve("secret.txt").toFile();
		Files.writeString(secret.toPath(), XXE_SECRET_MARKER);
		final String xml = "<!DOCTYPE r [<!ENTITY xxe SYSTEM \"file://" + secret.toURI().toASCIIString() + "\">]><r>&xxe;</r>";
		final Document doc = parseWithAllowDoctype(xml);
		//expandEntityReferences=false : l'EntityReference reste dans le DOM, non expansée
		assertFalse(domToString(doc).contains(XXE_SECRET_MARKER));
	}

	@Test
	public void allowDoctypeDoesNotExpandExternalParameterEntity(@TempDir final Path tempDir) throws Exception {
		final File secret = tempDir.resolve("secret.txt").toFile();
		Files.writeString(secret.toPath(), XXE_SECRET_MARKER);
		final String fileUri = secret.toURI().toASCIIString();
		final String xml = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE r ["
				+ "<!ENTITY % file SYSTEM \"" + fileUri + "\">"
				+ "<!ENTITY % eval \"<!ENTITY xxe SYSTEM '" + fileUri + "'>&xxe;\"> "
				+ "%eval;"
				+ "]><r/>";
		try {
			final Document doc = parseWithAllowDoctype(xml);
			assertFalse(domToString(doc).contains(XXE_SECRET_MARKER));
		} catch (final SAXException | IOException e) {
			//si la résolution est bloquée par une erreur, le secret ne doit pas fuiter dans le message
			assertFalse(String.valueOf(e.getMessage()).contains(XXE_SECRET_MARKER));
		}
	}

	@Test
	@Timeout(60)
	public void allowDoctypeBillionLaughsIsBounded() throws ParserConfigurationException {
		try {
			final Document doc = parseWithAllowDoctype(billionLaughs());
			assertTrue(totalTextLength(doc.getDocumentElement()) < 100_000, "entity expansion must be bounded by secure processing");
		} catch (final SAXException | IOException e) {
			//borné par la limite d'expansion de secure processing : ok
		}
	}

	//----- non régression sur l'existant

	@Test
	public void secureXmlXXEByOwaspDocumentBuilderFactoryRejectsDoctype() throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		XmlUtil.secureXmlXXEByOwasp(factory);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		Assertions.assertThrows(SAXException.class, () -> builder.parse(new InputSource(new StringReader("<!DOCTYPE r [ ]><r/>"))));
	}

	@Test
	public void secureXmlXXEByOwaspSAXParserFactoryRejectsDoctype() throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		XmlUtil.secureXmlXXEByOwasp(factory);
		final SAXParser parser = factory.newSAXParser();
		Assertions.assertThrows(SAXException.class, () -> parser.parse(new InputSource(new StringReader("<!DOCTYPE r><r/>")), new DefaultHandler()));
	}

	@Test
	public void secureXmlXXEByOwaspSchemaFactory() throws Exception {
		final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		XmlUtil.secureXmlXXEByOwasp(factory);
		assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
	}

	@Test
	public void secureXmlXXEByOwaspTransformerFactory() throws Exception {
		final TransformerFactory factory = TransformerFactory.newInstance();
		XmlUtil.secureXmlXXEByOwasp(factory);
		assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
		assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
	}

	@Test
	public void validateXmlByXsdValid(@TempDir final Path tempDir) throws Exception {
		final Path xsd = tempDir.resolve("t.xsd");
		Files.writeString(xsd, "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"t\"><xs:complexType><xs:sequence><xs:element name=\"a\" type=\"xs:string\"/></xs:sequence></xs:complexType></xs:element></xs:schema>");
		final Path xml = tempDir.resolve("t.xml");
		Files.writeString(xml, "<t><a>ok</a></t>");
		XmlUtil.validateXmlByXsd(xml.toUri().toURL(), xsd.toUri().toURL());
	}

	@Test
	public void validateXmlByXsdInvalid(@TempDir final Path tempDir) throws Exception {
		final Path xsd = tempDir.resolve("t.xsd");
		Files.writeString(xsd, "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"><xs:element name=\"t\"><xs:complexType><xs:sequence><xs:element name=\"a\" type=\"xs:string\"/></xs:sequence></xs:complexType></xs:element></xs:schema>");
		final Path xml = tempDir.resolve("t.xml");
		Files.writeString(xml, "<t><b/></t>");
		Assertions.assertThrows(WrappedException.class, () -> XmlUtil.validateXmlByXsd(xml.toUri().toURL(), xsd.toUri().toURL()));
	}

	//----- helpers de test

	private static Document tryParseXmlSecure(final String xml) {
		try {
			return XmlUtil.parseXmlSecure(xml);
		} catch (final RuntimeException e) {
			return null;
		}
	}

	private static Document parseWithAllowDoctype(final String xml) throws SAXException, IOException, ParserConfigurationException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		XmlUtil.secureXmlXXEByOwaspAllowDoctype(factory);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
		return builder.parse(new InputSource(new StringReader(xml)));
	}

	private static String billionLaughs() {
		final StringBuilder sb = new StringBuilder("<!DOCTYPE lolz [");
		sb.append("<!ENTITY lol \"lol\">");
		for (int level = 1; level < 9; level++) {
			sb.append("<!ENTITY lol").append(level + 1).append(" \"");
			for (int i = 0; i < 10; i++) {
				sb.append("&lol").append(level).append(";");
			}
			sb.append("\">");
		}
		sb.append("]><lolz>&lol9;</lolz>");
		return sb.toString();
	}

	private static String domToString(final Document doc) {
		final StringBuilder sb = new StringBuilder();
		for (Node child = doc.getFirstChild(); child != null; child = child.getNextSibling()) {
			appendNodeText(child, sb);
		}
		return sb.toString();
	}

	private static void appendNodeText(final Node node, final StringBuilder sb) {
		if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
			sb.append(node.getNodeValue());
			return;
		}
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			appendNodeText(child, sb);
		}
	}

	private static int totalTextLength(final Node node) {
		int length = 0;
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.TEXT_NODE) {
				length += child.getNodeValue().length();
			}
			length += totalTextLength(child);
		}
		return length;
	}

}
