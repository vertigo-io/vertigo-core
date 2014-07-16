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
package io.vertigo.xml;

import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Loader;
import io.vertigo.kernel.util.XMLUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * Parser XML du paramétrage de l'application.
 * @author npiedeloup, pchretien
 */
public final class XMLModulesLoader implements Loader<ComponentSpaceConfigBuilder> {
	private final URL managersURL;
	private final Properties properties;

	/**
	 * Builder de HomeConfig à partir d'une description XML et d'un fichier de propriétés.
	 */
	public XMLModulesLoader(final URL managersURL, final Properties properties) {
		Assertion.checkNotNull(managersURL);
		Assertion.checkNotNull(properties);
		//----------------------------------------------------------------------
		this.managersURL = managersURL;
		this.properties = properties;
	}

	/**
	 * Charge une configuration, et complète celle existante.
	 */
	public void load(ComponentSpaceConfigBuilder componentSpaceConfigBuilder) {
		Assertion.checkNotNull(componentSpaceConfigBuilder);
		//----------------------------------------------------------------------
		try {
			doLoad(componentSpaceConfigBuilder);
		} catch (final ParserConfigurationException pce) {
			throw new RuntimeException("Erreur de configuration du parseur (fichier " + managersURL.getPath() + "), lors de l'appel à newSAXParser()", pce);
		} catch (final SAXException se) {
			throw new RuntimeException("Erreur de parsing (fichier " + managersURL.getPath() + "), lors de l'appel à parse()", se);
		} catch (final IOException ioe) {
			throw new RuntimeException("Erreur d'entrée/sortie (fichier " + managersURL.getPath() + "), lors de l'appel à parse()", ioe);
		}
	}

	private void doLoad(ComponentSpaceConfigBuilder componentSpaceConfigBuilder) throws ParserConfigurationException, SAXException, IOException {
		//---validation XSD
		final URL xsd = XMLModulesLoader.class.getResource("vertigo_1_0.xsd");
		XMLUtil.validateXmlByXsd(managersURL, xsd);
		//---fin validation XSD

		final XMLModulesHandler handler = new XMLModulesHandler(componentSpaceConfigBuilder, properties);
		final SAXParserFactory factory = SAXParserFactory.newInstance();

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(managersURL.openStream()), handler);

		for (Object key : properties.keySet()) {
			componentSpaceConfigBuilder.withParam((String) key, properties.getProperty((String) key));
		}
	}
}
