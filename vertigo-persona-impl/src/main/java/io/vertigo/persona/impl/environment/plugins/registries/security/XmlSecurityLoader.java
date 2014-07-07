package io.vertigo.persona.impl.environment.plugins.registries.security;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.persona.security.model.Operation;
import io.vertigo.persona.security.model.Permission;
import io.vertigo.persona.security.model.Resource;
import io.vertigo.persona.security.model.Role;

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
 * Plugin XML chargeant la registry � partir d'un fichier XML
 * La d�finition du fichier XML est d�crite dans le fichier authorisation-config_1_0.dtd
 * Un exemple de fichier:
 * 
 * <authorisation-config>	
 *	
 *	<!--  Op�rations -->
 *	<operation id="read" description="Lire"/>
 *	<operation id="write" description="Ecrire"/>
 *	
 *	<!--  Ressources -->	
 *	<resource id="all_products" filter="/products/.*" description="Liste des produits"/>
 *	
 *	<!--  Permissions -->	
 *	<permission id="read_all_products" operation="read" resource="all_products" description="Lire tous les produits"/>		
 *	<permission id="write_all_products" operation="write" resource="all_products" description="Cr�er/Modifier un produit"/>				
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
 * @version $Id: XmlSecurityLoader.java,v 1.5 2014/02/27 10:34:47 pchretien Exp $
 */
final class XmlSecurityLoader {
	private static final String OPERATION_KEY = "operation";
	private static final String RESOURCE_KEY = "resource";
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
		// ---------------------------------------------------------------------
		authURL = resourceManager.resolve(url);
	}

	void load() {
		final Element root = XmlSecurityLoader.create(authURL, "/io/vertigo/persona/security/authorisation-config_1_0.dtd");

		// Operations
		final List<Element> opeElementList = root.getChildren(OPERATION_KEY);
		for (final Element opeElement : opeElementList) {
			final Operation operation = createOperation(opeElement);
			Home.getDefinitionSpace().put(operation, Operation.class);
		}
		// Ressources
		final List<Element> rscElementList = root.getChildren(RESOURCE_KEY);
		for (final Element resourceElement : rscElementList) {
			final Resource resource = createResource(resourceElement);
			Home.getDefinitionSpace().put(resource, Resource.class);
		}
		// Permission
		final List<Element> permElementList = root.getChildren(PERMISSION_KEY);
		for (final Element permElement : permElementList) {
			final Permission permission = createPermission(permElement);
			Home.getDefinitionSpace().put(permission, Permission.class);
		}
		// Role
		final List<Element> list = root.getChildren(ROLE_KEY);
		for (final Element roleElement : list) {
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
		//-----------------------------------------------------------------
		final EntityResolver entityResolver = new EntityResolver() {
			public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
				return new InputSource(getClass().getResourceAsStream(dtdResource));
			}
		};
		return createDocument(url, entityResolver).getRootElement();
	}

	//repris de XMLUtil 6.1.18
	private static Document createDocument(final URL url, final EntityResolver entityResolver) {
		Assertion.checkNotNull(url);
		//---------------------------------------------------------------------
		try {
			final SAXBuilder builder = new SAXBuilder(true);
			builder.setExpandEntities(true);
			builder.setEntityResolver(entityResolver);
			return builder.build(url.openStream());
		} catch (final Exception e) {
			throw new VRuntimeException("Erreur durant la lecture du fichier XML {0}", e, url);
		}
	}

	private static Operation createOperation(final Element opeElement) {
		Assertion.checkNotNull(opeElement);
		// ---------------------------------------------------------------------
		final String id = opeElement.getAttributeValue(ID_KEY);
		Assertion.checkArgNotEmpty(id);
		// ---------------------------------------------------------------------
		final String description = opeElement.getAttributeValue(DESCRIPTION_KEY);
		Assertion.checkArgNotEmpty(description);
		return new Operation(id, description);
	}

	private static Resource createResource(final Element rscElement) {
		Assertion.checkNotNull(rscElement);
		// ---------------------------------------------------------------------
		final String id = rscElement.getAttributeValue(ID_KEY);
		// ---------------------------------------------------------------------
		final String filter = rscElement.getAttributeValue(FILTER_KEY);
		// ---------------------------------------------------------------------
		final String description = rscElement.getAttributeValue(DESCRIPTION_KEY);
		// ---------------------------------------------------------------------
		return new Resource(id, filter, description);
	}

	private Permission createPermission(final Element permElement) {
		Assertion.checkNotNull(permElement);
		// ---------------------------------------------------------------------
		final String id = permElement.getAttributeValue(ID_KEY);
		// ---------------------------------------------------------------------
		final String operationRef = permElement.getAttributeValue(OPERATION_KEY);
		final Operation operation = Home.getDefinitionSpace().resolve(operationRef, Operation.class);
		// ---------------------------------------------------------------------
		final String resourceRef = permElement.getAttributeValue(RESOURCE_KEY);
		final Resource resource = Home.getDefinitionSpace().resolve(resourceRef, Resource.class);
		// ---------------------------------------------------------------------
		return new Permission(id, operation, resource);
	}

	private Role createRole(final Element roleElement) {
		Assertion.checkNotNull(roleElement);
		// ---------------------------------------------------------------------
		final String name = roleElement.getAttributeValue(NAME_KEY);
		// ---------------------------------------------------------------------
		final String description = roleElement.getAttributeValue(DESCRIPTION_KEY);
		// ---------------------------------------------------------------------
		final List<Permission> permissionList = new ArrayList<>();
		final List<Element> xps = roleElement.getChildren(PERMISSION_KEY);
		for (final Element element : xps) {
			final String permissionRef = element.getAttributeValue(REF_KEY);
			// ---------------------------------------------------------------------
			final Permission permission = Home.getDefinitionSpace().resolve(permissionRef, Permission.class);
			// ---------------------------------------------------------------------
			permissionList.add(permission);
		}
		return new Role(name, description, permissionList);
	}
}
