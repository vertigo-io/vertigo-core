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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

/**
 * XML utility : XSD validation and XXE hardening.
 * @author pchretien
 */
public final class XmlUtil {

	private static final String FEATURE_DISABLE_DTD = "http://apache.org/xml/features/disallow-doctype-decl";
	private static final String INTERNAL_XML_SCHEMA_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory";
	private static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

	/**
	 * Constructor.
	 */
	private XmlUtil() {
		//private constructor
	}

	/**
	 * Removes a leading DOCTYPE declaration (including its internal subset) from an XML content,
	 * so that the document can be parsed with strict XXE settings (disallow-doctype-decl).
	 * <br>
	 * The DOCTYPE keyword is matched case-insensitively. Only a DOCTYPE positioned right after an optional
	 * BOM, whitespaces and an XML declaration (&lt;?xml ...?&gt;) is removed : this is the only position where a
	 * DOCTYPE declaration is legal in XML. The input is returned unchanged when no DOCTYPE is found, when the
	 * DOCTYPE is not at this initial position, or when the declaration is not properly terminated
	 * (the XML parser will then report the underlying error). Nothing else is ever modified.
	 * @param xml XML content
	 * @return the same content without its leading DOCTYPE declaration, or the input unchanged
	 * @throws NullPointerException if xml is null
	 */
	public static String removeDoctype(final String xml) {
		Assertion.check().isNotNull(xml);
		//---
		final int doctypeStart = findDoctypeStart(xml);
		if (doctypeStart < 0) {
			return xml;
		}
		final int doctypeEnd = endDoctype(xml, doctypeStart);
		if (doctypeEnd < 0) {
			return xml;
		}
		return xml.substring(0, doctypeStart) + xml.substring(doctypeEnd);
	}

	private static int findDoctypeStart(final String xml) {
		final int length = xml.length();
		int i = 0;
		if (length > 0 && xml.charAt(0) == '\uFEFF') {
			i = 1;
		}
		while (i < length) {
			final char c = xml.charAt(i);
			if (Character.isWhitespace(c)) {
				i++;
				continue;
			}
			if (c == '<' && xml.regionMatches(true, i, "<?xml", 0, 5) && i + 5 < length && Character.isWhitespace(xml.charAt(i + 5))) {
				final int end = xml.indexOf("?>", i + 5);
				if (end < 0) {
					return -1;
				}
				i = end + 2;
				continue;
			}
			break;
		}
		if (!xml.regionMatches(true, i, "<!DOCTYPE", 0, 9)) {
			return -1;
		}
		return i;
	}

	private static int endDoctype(final String xml, final int doctypeStart) {
		final int length = xml.length();
		int i = doctypeStart + 9;
		if (i >= length || !Character.isWhitespace(xml.charAt(i))) {
			return -1;
		}
		i = skipWhitespace(xml, i);
		final int nameStart = i;
		while (i < length && isNameChar(xml.charAt(i))) {
			i++;
		}
		if (i == nameStart) {
			return -1;
		}
		i = skipWhitespace(xml, i);
		if (i >= length) {
			return -1;
		}
		if (xml.charAt(i) == '[') {
			return endInternalSubset(xml, i);
		}
		if (xml.regionMatches(true, i, "PUBLIC", 0, 6)) {
			i = skipQuotedString(xml, skipWhitespace(xml, i + 6));
			if (i < 0) {
				return -1;
			}
			i = skipQuotedString(xml, skipWhitespace(xml, i));
			if (i < 0) {
				return -1;
			}
			i = skipWhitespace(xml, i);
		} else if (xml.regionMatches(true, i, "SYSTEM", 0, 6)) {
			i = skipQuotedString(xml, skipWhitespace(xml, i + 6));
			if (i < 0) {
				return -1;
			}
			i = skipWhitespace(xml, i);
		} else {
			if (xml.charAt(i) != '>') {
				return -1;
			}
			return i + 1;
		}
		if (i >= length) {
			return -1;
		}
		if (xml.charAt(i) == '[') {
			return endInternalSubset(xml, i);
		}
		if (xml.charAt(i) != '>') {
			return -1;
		}
		return i + 1;
	}

	private static int skipWhitespace(final String xml, final int from) {
		int i = from;
		while (i < xml.length() && Character.isWhitespace(xml.charAt(i))) {
			i++;
		}
		return i;
	}

	private static int skipQuotedString(final String xml, final int from) {
		if (from >= xml.length()) {
			return -1;
		}
		final char quote = xml.charAt(from);
		if (quote != '"' && quote != '\'') {
			return -1;
		}
		final int end = xml.indexOf(quote, from + 1);
		return end < 0 ? -1 : end + 1;
	}

	private static int endInternalSubset(final String xml, final int from) {
		//from points to the '[' opening the internal subset, returns the index after the closing '>'
		int depth = 1;
		int i = from + 1;
		final int length = xml.length();
		while (i < length) {
			final char c = xml.charAt(i);
			if (c == '"' || c == '\'') {
				final int end = xml.indexOf(c, i + 1);
				if (end < 0) {
					return -1;
				}
				i = end + 1;
				continue;
			}
			if (c == '<' && xml.regionMatches(i, "<![CDATA[", 0, 9)) {
				final int end = xml.indexOf("]]>", i + 9);
				if (end < 0) {
					return -1;
				}
				i = end + 3;
				continue;
			}
			if (c == '[') {
				depth++;
			} else if (c == ']') {
				depth--;
				if (depth == 0) {
					i++;
					break;
				}
			}
			i++;
		}
		if (depth != 0) {
			return -1;
		}
		i = skipWhitespace(xml, i);
		if (i >= length || xml.charAt(i) != '>') {
			return -1;
		}
		return i + 1;
	}

	private static boolean isNameChar(final char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.';
	}

	/**
	 * Secure XML parser with OWASP recommendations.
	 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
	 * @param schemaFactory SchemaFactory
	 * @throws SAXNotRecognizedException
	 * @throws SAXNotSupportedException
	 */
	public static void secureXmlXXEByOwasp(final SchemaFactory schemaFactory) throws SAXNotRecognizedException, SAXNotSupportedException {
		Assertion.check().isNotNull(schemaFactory);
		//---
		schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
	}

	/**
	 * Secure XML parser with OWASP recommendations.
	 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
	 * @param schemaFactory SchemaFactory
	 * @throws ParserConfigurationException
	 * @throws SAXNotRecognizedException
	 * @throws SAXNotSupportedException
	 */
	public static void secureXmlXXEByOwasp(final SAXParserFactory factory) throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
		Assertion.check().isNotNull(factory);
		//---
		factory.setFeature(FEATURE_DISABLE_DTD, true);
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
		factory.setXIncludeAware(false);
	}

	/**
	 * Secure XML parser with OWASP recommendations.
	 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
	 *      If error, you may use an other XML lib, check: https://stackoverflow.com/questions/45152707/transformerfactory-and-xalan-dependency-conflict/64364531#64364531
	 * @param tf TransformerFactory
	 * @throws TransformerConfigurationException
	 */
	public static void secureXmlXXEByOwasp(final TransformerFactory tf) throws TransformerConfigurationException {
		Assertion.check().isNotNull(tf);
		//---
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
	}

	/**
	 * Secure XML parser with OWASP recommendations.
	 * Documents containing a DOCTYPE declaration are rejected : see {@link #removeDoctype(String)} / {@link #parseXmlSecure(String)}.
	 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
	 * @param domFactory DocumentBuilderFactory
	 * @throws ParserConfigurationException
	 */
	public static void secureXmlXXEByOwasp(final DocumentBuilderFactory documentBuilderFactory) throws ParserConfigurationException {
		Assertion.check().isNotNull(documentBuilderFactory);
		//---
		documentBuilderFactory.setFeature(FEATURE_DISABLE_DTD, true);
		documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		documentBuilderFactory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
		documentBuilderFactory.setXIncludeAware(false);
		documentBuilderFactory.setExpandEntityReferences(false);
	}

	/**
	 * Fallback pour documents imposant un DOCTYPE (fichiers reçus non modifiables).
	 * Risque résiduel documenté par l'OWASP : SSRF et DoS (billion laughs, décompression jar:).
	 * Préférer {@link #secureXmlXXEByOwasp(DocumentBuilderFactory)} + {@link #removeDoctype(String)} / {@link #parseXmlSecure(String)} dès que possible.
	 * Note : expandEntityReferences=false laisse les EntityReference non expansées dans le DOM.
	 * Défense en profondeur : définir aussi sur le DocumentBuilder issu de cette factory un EntityResolver renvoyant une InputSource vide
	 * (builder.setEntityResolver((publicId, systemId) -&gt; new InputSource(new StringReader("")))).
	 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
	 * @param documentBuilderFactory DocumentBuilderFactory
	 * @throws ParserConfigurationException
	 */
	public static void secureXmlXXEByOwaspAllowDoctype(final DocumentBuilderFactory documentBuilderFactory) throws ParserConfigurationException {
		Assertion.check().isNotNull(documentBuilderFactory);
		//---
		documentBuilderFactory.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
		documentBuilderFactory.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
		documentBuilderFactory.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
		documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		documentBuilderFactory.setXIncludeAware(false);
		documentBuilderFactory.setExpandEntityReferences(false);
	}

	/**
	 * Parses an XML document securely (OWASP strict mode) after removing a leading DOCTYPE declaration.
	 * Use it for documents that must be parseable while carrying a residual &lt;!DOCTYPE&gt; (e.g. generated XHTML).
	 * @param xml XML content
	 * @return parsed Document (without any DOCTYPE)
	 * @throws NullPointerException if xml is null
	 */
	public static Document parseXmlSecure(final String xml) {
		Assertion.check().isNotNull(xml);
		return parseXmlStrict(removeDoctype(xml));
	}

	/**
	 * Parses an XML document securely (OWASP strict mode) after removing a leading DOCTYPE declaration.
	 * The stream is fully read using UTF-8 : an encoding declared in the XML prolog is ignored.
	 * @param xml XML content (UTF-8)
	 * @return parsed Document (without any DOCTYPE)
	 * @throws NullPointerException if xml is null
	 */
	public static Document parseXmlSecure(final InputStream xml) {
		Assertion.check().isNotNull(xml);
		try (xml) {
			return parseXmlSecure(new String(xml.readAllBytes(), StandardCharsets.UTF_8));
		} catch (final IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	private static Document parseXmlStrict(final String xml) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			secureXmlXXEByOwasp(factory);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (final SAXException | IOException | ParserConfigurationException e) {
			throw WrappedException.wrap(e);
		}
	}

	/**
	 * Util to validate XML with a XSD.
	 * @param xsd XSD
	 * @param xml XML to validate
	 */
	public static void validateXmlByXsd(final URL xml, final URL xsd) {
		Assertion.check()
				.isNotNull(xml)
				.isNotNull(xsd);
		//---
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI, INTERNAL_XML_SCHEMA_FACTORY, Thread.currentThread().getContextClassLoader());
		try {
			secureXmlXXEByOwasp(schemaFactory);
			final Validator validator = schemaFactory
					.newSchema(xsd)
					.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			//unsupported by xerces : validator.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			final Source source = new StreamSource(xml.openStream());
			validator.validate(source);
		} catch (final SocketException e) {
			throw WrappedException.wrap(e, "'{0}' may refer an DTD, you should removed <!DOCTYPE header tag", xml);
		} catch (final SAXException | IOException e) {
			throw WrappedException.wrap(e);
		}
	}

}
