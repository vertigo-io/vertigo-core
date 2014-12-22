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
package io.vertigo.persona.plugins.security.loaders;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
 */
final class XmlSecurityLoader {
	private static final String OPERATION_KEY = "operation";
	private static final String PERMISSION_KEY = "permission";
	private static final String ROLE_KEY = "role";

	private static final String ID_KEY = "id";
	private static final String REF_KEY = "ref";
	private static final String NAME_KEY = "name";
	private static final String FILTER_KEY = "filter";
	private static final String DESCRIPTION_KEY = "description";
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

	void load() {
		final Element root = XmlSecurityLoader.create(authURL, "/io/vertigo/persona/security/authorisation-config_1_0.dtd");

		// Permission
		final List<Element> permissions = root.getChildren(PERMISSION_KEY);
		for (final Element permissionElement : permissions) {
			final Permission permission = createPermission(permissionElement);
			Home.getDefinitionSpace().put(permission, Permission.class);
		}
		// Role
		final List<Element> roleKeys = root.getChildren(ROLE_KEY);
		for (final Element roleElement : roleKeys) {
			final Role role = createRole(roleElement);
			Home.getDefinitionSpace().put(role, Role.class);
		}
	}

	/**
	 * Reads an XML file and creates the root element
	 * @param url the url of the xml file to be read
	 * @return the root element
	 */
	//repris de XMLUtil 6.1.18
	private static Element create(final URL url, final String dtdResource) {
		Assertion.checkArgNotEmpty(dtdResource);
		//-----
		final EntityResolver entityResolver = new EntityResolver() {
			@Override
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				return new InputSource(getClass().getResourceAsStream(dtdResource));
			}
		};
		return createDocument(url, entityResolver).getRootElement();
	}

	//repris de XMLUtil 6.1.18
	private static Document createDocument(final URL url, final EntityResolver entityResolver) {
		Assertion.checkNotNull(url);
		//-----
		try {
			final SAXBuilder builder = new SAXBuilder(true);
			builder.setExpandEntities(true);
			builder.setEntityResolver(entityResolver);
			return builder.build(url.openStream());
		} catch (final Exception e) {
			throw new RuntimeException("Erreur durant la lecture du fichier XML " + url, e);
		}
	}

	private static Permission createPermission(final Element permissionElement) {
		Assertion.checkNotNull(permissionElement);
		//-----
		final String id = permissionElement.getAttributeValue(ID_KEY);
		final String operation = permissionElement.getAttributeValue(OPERATION_KEY);
		final String filter = permissionElement.getAttributeValue(FILTER_KEY);
		//-----
		return new Permission(id, operation, filter);
	}

	private static Role createRole(final Element roleElement) {
		Assertion.checkNotNull(roleElement);
		//-----
		final String name = roleElement.getAttributeValue(NAME_KEY);
		//-----
		final String description = roleElement.getAttributeValue(DESCRIPTION_KEY);
		//-----
		final List<Permission> permissions = new ArrayList<>();
		final List<Element> xps = roleElement.getChildren(PERMISSION_KEY);
		for (final Element element : xps) {
			final String permissionRef = element.getAttributeValue(REF_KEY);
			//-----
			final Permission permission = Home.getDefinitionSpace().resolve(permissionRef, Permission.class);
			//-----
			permissions.add(permission);
		}
		return new Role(name, description, permissions);
	}
}
