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
package io.vertigo.persona.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Role;

/**
 * Session d'un utilisateur.
 * Un utilisateur
 * <ul>
 * <li>est authentifie ou non,</li>
 * <li>possede une liste de roles (prealablement enregistres dans la RoleRegistry),</li>
 * <li>possède une liste d'attributs serialisables</li>.
 * </ul>
 *
 * @author alauthier, pchretien
 */
public abstract class PersonaUserSession extends UserSession {

	private static final long serialVersionUID = -7595168798429972117L;

	/**
	 * Set des roles autorises pour la session utilisateur.
	 */
	private final Set<DefinitionReference<Role>> roleRefs = new HashSet<>();

	//===========================================================================
	//=======================GESTION DES ROLES===================================
	//===========================================================================
	/**
	 * Ajoute un role pour l'utilisateur courant.
	 * Le role doit avoir ete prealablement enregistre.
	 *
	 * @param role Role e ajouter.
	 * @return the user session (for fluent)
	 */
	public final PersonaUserSession addRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		roleRefs.add(new DefinitionReference<>(role));
		return this;
	}

	/**
	 * Retourne la liste des roles de securite pour l'utilisateur.
	 *
	 * @return Set des roles.
	 */
	public final Set<Role> getRoles() {
		final Set<Role> roleSet = new HashSet<>();
		for (final DefinitionReference<Role> roleReference : roleRefs) {
			roleSet.add(roleReference.get());
		}
		return Collections.unmodifiableSet(roleSet);
	}

	/**
	 * @param role Role
	 * @return Vrai si le role est present
	 */
	public final boolean hasRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		return roleRefs.contains(new DefinitionReference<>(role));
	}

	/**
	 * Retrait de tous les roles possedes par l'utilisateur.
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public final void clearRoles() {
		roleRefs.clear();
	}

	//===========================================================================
	//=======================GESTION DES AUTHENTIFICATIONS=======================
	//===========================================================================

	/**
	 * Gestion multilingue.
	 * Local associée à l'utilisateur.
	 * @return Locale associée à l'utilisateur.
	 */
	@Override
	public abstract Locale getLocale();

	/**
	 * Gestion de la sécurité.
	 * @return Liste des clés de sécurité et leur valeur.
	 */
	public Map<String, String> getSecurityKeys() {
		return Collections.emptyMap();
	}
}
