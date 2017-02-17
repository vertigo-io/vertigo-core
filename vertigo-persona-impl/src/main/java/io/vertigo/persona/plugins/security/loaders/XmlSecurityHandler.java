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
package io.vertigo.persona.plugins.security.loaders;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;

/**
 * @author  pchretien
 */
final class XmlSecurityHandler extends DefaultHandler {
	private enum TagName {
		authorisationConfig,
		permission,
		role;
	}

	private enum AttrsName {
		id,
		operation,
		filter,
		description,
		permission,
		name,
		ref;
	}

	private final DefinitionSpace definitionSpace;
	private Permission currentPermission;
	private Role currentRole;

	XmlSecurityHandler(final DefinitionSpace definitionSpace) {
		Assertion.checkNotNull(definitionSpace);
		//-----
		this.definitionSpace = definitionSpace;
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		switch (TagName.valueOf(qName)) {
			case authorisationConfig:
				break;
			case permission:
				currentPermission = createPermission(attrs);
				break;
			case role:
				currentRole = createRole(attrs);
				break;
			default:
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		switch (TagName.valueOf(qName)) {
			case authorisationConfig:
				break;
			case permission:
				if (currentRole != null) {
					currentRole.getPermissions().add(currentPermission);
				}
				currentPermission = null;
				break;
			case role:
				definitionSpace.registerDefinition(currentRole);
				currentRole = null;
				break;
			default:
		}
	}

	private static Role createRole(final Attributes attrs) {
		Assertion.checkNotNull(attrs);
		//-----
		final String name = attrs.getValue(AttrsName.name.name()).trim();
		final String description = attrs.getValue(AttrsName.description.name()).trim();
		//-----
		final List<Permission> permissions = new ArrayList<>();
		return new Role(name, description, permissions);
	}

	//case of <permission id="PRM_READ_ALL_PRODUCTS" operation="READ" filter="/products/.*" description="Lire tous les produits"/>
	private Permission createPermission(final Attributes attrs) {
		Assertion.checkNotNull(attrs);
		//-----
		final String permissionRef = attrs.getValue(AttrsName.ref.name());
		if (permissionRef != null) {
			return obtainPermission(permissionRef);
		}
		final String id = attrs.getValue(AttrsName.id.name()).trim();
		final String operation = attrs.getValue(AttrsName.operation.name());
		final String filter = attrs.getValue(AttrsName.filter.name());
		//-----
		final Permission permission = new Permission(id, operation, filter);
		definitionSpace.registerDefinition(permission);
		return permission;
	}

	//case of <permission ref="PRM_READ_MY_FAMILLE"/>
	private Permission obtainPermission(final String permissionRef) {
		Assertion.checkArgNotEmpty(permissionRef);
		//-----
		return definitionSpace.resolve(permissionRef, Permission.class);
	}

}
