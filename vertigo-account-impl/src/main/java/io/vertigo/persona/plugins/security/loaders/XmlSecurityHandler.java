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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;

/**
 * @author  pchretien
 * @deprecated Use new account security management instead
 */
@Deprecated
final class XmlSecurityHandler extends DefaultHandler {
	private enum TagName {
		authorisationConfig, permission, role;
	}

	private enum AttrsName {
		id, operation, filter, description, permission, name, ref;
	}

	private final List<DefinitionSupplier> permissionSuppliers = new ArrayList<>();
	private final List<DefinitionSupplier> roleSuppliers = new ArrayList<>();
	private final List<String> permissionsRef = new ArrayList<>();
	private final String[] currentRoleAttributes = new String[2];
	private boolean isInRole;

	List<DefinitionSupplier> getPermissionSuppliers() {
		return permissionSuppliers;
	}

	List<DefinitionSupplier> getRoleSuppliers() {
		return roleSuppliers;
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		switch (TagName.valueOf(qName)) {
			case authorisationConfig:
				break;
			case permission:
				if (!isInRole) {
					// it's a real permission so we handle it
					permissionSuppliers.add(supplyPermissions(
							attrs.getValue(AttrsName.id.name()).trim(),
							attrs.getValue(AttrsName.operation.name()).trim(),
							attrs.getValue(AttrsName.filter.name()).trim()));
				} else {
					// we are in a role so we append to the list of references
					permissionsRef.add(attrs.getValue(AttrsName.ref.name()));
				}
				break;
			case role:
				isInRole = true;
				currentRoleAttributes[0] = attrs.getValue(AttrsName.name.name()).trim();
				currentRoleAttributes[1] = attrs.getValue(AttrsName.description.name()).trim();
				break;
			default:
		}
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		switch (TagName.valueOf(qName)) {
			case authorisationConfig:
			case permission:
				break;
			case role:
				Assertion.checkNotNull(currentRoleAttributes);
				// ---
				roleSuppliers.add(supplyRole(
						currentRoleAttributes[0],
						currentRoleAttributes[1],
						new ArrayList<>(permissionsRef)));
				permissionsRef.clear();
				isInRole = false;
				break;
			default:
		}
	}

	private static DefinitionSupplier supplyRole(final String name, final String description, final List<String> myPermRefs) {
		return definitionSpace -> createRole(name, description, myPermRefs, definitionSpace);
	}

	private static Role createRole(final String name, final String description, final List<String> myPermRefs, final DefinitionSpace definitionSpace) {
		final List<Permission> permissions = myPermRefs.stream()
				.map(permissionName -> definitionSpace.resolve(permissionName, Permission.class))
				.collect(Collectors.toList());
		return new Role(name, description, permissions);
	}

	//case of <permission id="PRM_READ_ALL_PRODUCTSè" operation="READ" filter="/products/.*" description="Lire tous les produits"/>
	private static DefinitionSupplier supplyPermissions(final String id, final String operation, final String filter) {
		return definitionSpace -> new Permission(id, operation, filter);
	}
}
