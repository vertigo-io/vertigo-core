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
package io.vertigoimpl.plugins.commons.config.xml;

import io.vertigo.commons.config.ConfigManager;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.XMLUtil;
import io.vertigoimpl.commons.config.ConfigPlugin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * Parser XML du paramétrage de la config.
 * @author  pchretien
 */
public final class XmlConfigPlugin implements ConfigPlugin { /*implements Loader<HomeConfigBuilder>*/
	//	private final URL managersURL;
	//	private final Properties properties;
	private final Map<String, Map<String, String>> configs;

	/**
	 * Constructeur
	 * @param resourceManager Selector
	 * @param url Url du fichier XML de configuration
	 */
	@Inject
	public XmlConfigPlugin(final ResourceManager resourceManager, @Named("url") final String url) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(url);
		// ---------------------------------------------------------------------
		final URL configURL = resourceManager.resolve(url);
		configs = readXML(configURL);
	}

	/** {@inheritDoc} */
	public Option<String> getValue(final String configPath, final String propertyName) {
		Assertion.checkArgNotEmpty(configPath);
		Assertion.checkArgNotEmpty(propertyName);
		// ---------------------------------------------------------------------
		final Map<String, String> properties = configs.get(configPath);
		return properties == null ? Option.<String> none() : Option.<String> option(properties.get(propertyName));
	}

	/**
	 * Charge une configuration, et complète celle existante.
	 */
	private static Map<String, Map<String, String>> readXML(final URL configURL) {
		Assertion.checkNotNull(configURL);
		//----------------------------------------------------------------------
		try {
			return doReadXML(configURL);
		} catch (final ParserConfigurationException pce) {
			throw new VRuntimeException("Erreur de configuration du parseur (fichier {0}), lors de l'appel à newSAXParser()", pce, configURL.getPath());
		} catch (final SAXException se) {
			throw new VRuntimeException("Erreur de parsing (fichier {0}), lors de l'appel à parse()", se, configURL.getPath());
		} catch (final IOException ioe) {
			throw new VRuntimeException("Erreur d'entrée/sortie (fichier {0}), lors de l'appel à parse()", ioe, configURL.getPath());
		}
	}

	private static Map<String, Map<String, String>> doReadXML(final URL configURL) throws SAXException, IOException, ParserConfigurationException {
		xsdValidate(configURL);
		//---
		final Map<String, Map<String, String>> tmpConfigs = new HashMap<>();

		final XmlConfigHandler handler = new XmlConfigHandler(tmpConfigs);
		final SAXParserFactory factory = SAXParserFactory.newInstance();

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(configURL.openStream()), handler);
		return tmpConfigs;
	}

	private static void xsdValidate(final URL configURL) {
		//---validation XSD
		final URL xsd = ConfigManager.class.getResource("vertigo-config_1_0.xsd");
		XMLUtil.validateXmlByXsd(configURL, xsd);
		//---fin validation XSD}
	}

}
