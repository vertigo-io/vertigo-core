/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app.config.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.XMLUtil;

/**
 * Parser XML du paramétrage de l'application.
 * @author npiedeloup, pchretien
 */
final class XMLModulesParser {

	private XMLModulesParser() {
		//
	}

	/**
	 * Parser à partir d'une description XML et d'un fichier de propriétés.
	 */
	static void parseAll(final AppConfigBuilder appConfigBuilder, final Properties properties, final List<URL> managersURLs) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkNotNull(managersURLs);
		Assertion.checkNotNull(properties);
		//-----
		final XMLModulesParams params = new XMLModulesParams(properties);
		for (final URL managersURL : managersURLs) {
			parse(appConfigBuilder, managersURL, params);
		}
		//-----
		Assertion.checkArgument(params.unreadProperties().isEmpty(), "Some boot properties are unused {0}; Check they must starts with 'boot.'", params.unreadProperties());
	}

	private static void parse(final AppConfigBuilder appConfigBuilder, final URL managersURL, final XMLModulesParams params) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkNotNull(managersURL);
		//-----
		try {
			doParse(appConfigBuilder, managersURL, params);
		} catch (final ParserConfigurationException pce) {
			throw WrappedException.wrap(pce, "Erreur de configuration du parseur (fichier " + managersURL.getPath() + "), lors de l'appel à newSAXParser()");
		} catch (final SAXException se) {
			throw WrappedException.wrap(se, "Erreur de parsing (fichier " + managersURL.getPath() + "), lors de l'appel à parse()");
		} catch (final IOException ioe) {
			throw WrappedException.wrap(ioe, "Erreur d'entrée/sortie (fichier " + managersURL.getPath() + "), lors de l'appel à parse()");
		}
	}

	private static void doParse(final AppConfigBuilder appConfigBuilder, final URL managersURL, final XMLModulesParams params) throws ParserConfigurationException, SAXException, IOException {
		//---validation XSD
		final URL xsd = XMLModulesParser.class.getResource("vertigo_1_0.xsd");
		XMLUtil.validateXmlByXsd(managersURL, xsd);
		//---fin validation XSD

		final XMLModulesHandler handler = new XMLModulesHandler(appConfigBuilder, params);
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(managersURL.openStream()), handler);
	}
}
