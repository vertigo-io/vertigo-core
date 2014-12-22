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
package io.vertigo.util;

import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * XSD Validation.
 * @author pchretien
 */
public final class XMLUtil {

	/**
	 * Constructeur.
	 */
	private XMLUtil() {
		//Rien
	}

	/**
	 * Validation XSD.
	 * @param xsd Validateur XSD
	 * @param xml XML à valider
	 */
	public static void validateXmlByXsd(final URL xml, final URL xsd) {
		Assertion.checkNotNull(xml);
		Assertion.checkNotNull(xsd);
		//-----
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			final Validator validator = schemaFactory.newSchema(xsd).newValidator();
			final StreamSource streamSource = new StreamSource(xml.openStream());
			try {
				validator.validate(streamSource);
			} catch (final SAXException e) {
				throw new RuntimeException("'" + xml.toString() + "' non valide", e);
			}
		} catch (final SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
