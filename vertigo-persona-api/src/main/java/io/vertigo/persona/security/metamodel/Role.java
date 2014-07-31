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
package io.vertigo.persona.security.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.stereotype.Prefix;

import java.util.List;


/**
 * Un rôle est la réunion d'un ensemble de permissions. 
 * Un utilisateur peut avoir  plusieurs rôles.
 * 
 * @author prahmoune
 * @version $Id: Role.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
 */
@Prefix("R_")
public final class Role implements Definition {
	private final String name;
	private final String description;
	private final List<Permission> permissions;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom du rôle
	 * @param description Description du rôle
	 * @param permissions Liste des permissions associées au rôle
	 */
	public Role(final String name, final String description, final List<Permission> permissions) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(description);
		Assertion.checkNotNull(permissions);
		// ---------------------------------------------------------------------
		this.name = name;
		this.description = description;
		this.permissions = permissions;
	}

	/**
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Liste des permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * @return Nom du rôle
	 */
	public String getName() {
		return name;
	}
}
