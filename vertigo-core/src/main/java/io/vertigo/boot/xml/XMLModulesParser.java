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
package io.vertigo.boot.xml;

import io.vertigo.core.config.ModuleConfig;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.XMLUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

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
	static List<ModuleConfig> parseAll(final Properties properties, final List<URL> managersURLs) {
		Assertion.checkNotNull(managersURLs);
		Assertion.checkNotNull(properties);
		//-----
		final XMLParams params = new XMLParams(properties);
		final List<ModuleConfig> moduleConfigs = new ArrayList<>();
		for (final URL managersURL : managersURLs) {
			moduleConfigs.addAll(parse(managersURL, params));
		}
		//-----
		Assertion.checkArgument(params.unreadProperties().isEmpty(), "Some properties are unused {0}", params.unreadProperties());
		//-----
		return moduleConfigs;
	}

	private static List<ModuleConfig> parse(final URL managersURL, final XMLParams params) {
		Assertion.checkNotNull(managersURL);
		//-----
		try {
			return doParse(managersURL, params);
		} catch (final ParserConfigurationException pce) {
			throw new RuntimeException("Erreur de configuration du parseur (fichier " + managersURL.getPath() + "), lors de l'appel à newSAXParser()", pce);
		} catch (final SAXException se) {
			throw new RuntimeException("Erreur de parsing (fichier " + managersURL.getPath() + "), lors de l'appel à parse()", se);
		} catch (final IOException ioe) {
			throw new RuntimeException("Erreur d'entrée/sortie (fichier " + managersURL.getPath() + "), lors de l'appel à parse()", ioe);
		}
	}

	private static List<ModuleConfig> doParse(final URL managersURL, final XMLParams params) throws ParserConfigurationException, SAXException, IOException {
		//---validation XSD
		final URL xsd = XMLModulesParser.class.getResource("vertigo_1_0.xsd");
		XMLUtil.validateXmlByXsd(managersURL, xsd);
		//---fin validation XSD

		final ListBuilder<ModuleConfig> moduleConfigsBuilder = new ListBuilder<>();
		final XMLModulesHandler handler = new XMLModulesHandler(moduleConfigsBuilder, params);
		final SAXParserFactory factory = SAXParserFactory.newInstance();

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(managersURL.openStream()), handler);

		return moduleConfigsBuilder.unmodifiable().build();
	}
}
