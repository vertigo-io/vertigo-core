/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.plugins.security.loaders;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Named;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.StringUtil;
import io.vertigo.util.XMLUtil;

/**
 * Plugin XML chargeant la registry à partir d'un fichier XML
 * La définition du fichier XML est décrite dans le fichier authorisation-config_1_0.dtd
 * Un exemple de fichier:
 *
 * <authorisation-config>
 *
 *	<!--  Ressources -->
 *	<resource id="all_products" filter="/products/.*" description="Liste des produits"/>
 *
 *	<!--  Permissions -->
 *	<permission id="read_all_products" operation="read" resource="all_products" description="Lire tous les produits"/>
 *	<permission id="write_all_products" operation="write" resource="all_products" description="Créer/Modifier un produit"/>
 *
 *	<!-- Roles -->
 *	<role name="reader" description="Lecteur de l'application">
 * 		<permission ref="read_all_products"/>
 *  	</role>
 *  	<role name="writer" description="Ecrivain de l'application">
 *  		<permission ref="read_all_products"/>
 *  		<permission ref="write_all_products"/>
 *  	</role>
 * </authorisation-config>
 * @author prahmoune
 * @deprecated Use new account security management instead
 */
@Deprecated
final class XmlSecurityLoader {
	private final URL authURL;

	/**
	 * Constructeur
	 * @param resourceManager Resource manager
	 * @param url Url du fichier XML de configuration
	 */
	XmlSecurityLoader(final ResourceManager resourceManager, @Named("url") final String url) {
		Assertion.checkNotNull(resourceManager);
		Assertion.checkArgNotEmpty(url);
		//-----
		authURL = resourceManager.resolve(url);
	}

	List<DefinitionSupplier> load() {
		Assertion.checkNotNull(authURL);
		//-----
		try {
			return doLoadXML(authURL);
		} catch (final ParserConfigurationException pce) {
			throw WrappedException.wrap(pce, StringUtil.format("Erreur de configuration du parseur (fichier {0}), lors de l'appel à newSAXParser()", authURL.getPath()));
		} catch (final SAXException se) {
			throw WrappedException.wrap(se, StringUtil.format("Erreur de parsing (fichier {0}), lors de l'appel à parse()", authURL.getPath()));
		} catch (final IOException ioe) {
			throw WrappedException.wrap(ioe, StringUtil.format("Erreur d'entrée/sortie (fichier {0}), lors de l'appel à parse()", authURL.getPath()));
		}
	}

	private static List<DefinitionSupplier> doLoadXML(final URL configURL) throws SAXException, IOException, ParserConfigurationException {
		xsdValidate(configURL);
		//---

		final XmlSecurityHandler handler = new XmlSecurityHandler();
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(new BufferedInputStream(configURL.openStream()), handler);
		return new ListBuilder<DefinitionSupplier>()
				.addAll(handler.getPermissionSuppliers())
				.addAll(handler.getRoleSuppliers())
				.build();
	}

	private static void xsdValidate(final URL configURL) {
		//--- validation XSD
		final URL xsd = VSecurityManager.class.getResource("vertigo-security_1_0.xsd");
		XMLUtil.validateXmlByXsd(configURL, xsd);
		//--- fin validation XSD
	}

}
