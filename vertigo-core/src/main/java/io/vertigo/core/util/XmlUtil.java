/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
import java.net.SocketException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

/**
 * XSD Validation.
 * @author pchretien
 */
public final class XmlUtil {

	private static final String FEATURE_DISABLE_DTD = "http://apache.org/xml/features/disallow-doctype-decl";
	private static final String INTERNAL_XML_SCHEMA_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory";
	private static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	/**
	 * Constructor.
	 */
	private XmlUtil() {
		//private constructor
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
