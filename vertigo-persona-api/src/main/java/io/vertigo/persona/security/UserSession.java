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
package io.vertigo.persona.security;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;
import io.vertigo.persona.security.metamodel.Role;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Session d'un utilisateur.
 * Un utilisateur
 * <ul>
 * <li>est authentifie ou non,</li>
 * <li>possede une liste de roles (prealablement enregistres dans la RoleRegistry),</li>
 * <li>poss�de une liste d'attributs serialisables</li>.
 * </ul>
 *
 * @author alauthier, pchretien
 * @version $Id: UserSession.java,v 1.5 2014/02/27 10:38:22 pchretien Exp $
 */
public abstract class UserSession implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 467617818948129397L;

	/**
	 * Cle de la session (utilise comme cle unique de connexion).
	 */
	private final UUID sessionUUID = createUUID();

	/**
	 * Set des roles autorises pour la session utilisateur.
	 */
	private final Set<DefinitionReference<Role>> roles = new HashSet<>();

	/**
	 * Indique si l'utilisateur est authentifie.
	 * Par defaut l'utilisateur n'est pas authentifie.
	 */
	private boolean authenticated;

	private UUID createUUID() {
		//On utilise le mecanisme de creation standard.
		return UUID.randomUUID();
	}

	//===========================================================================
	//=======================GESTION DES ROLES===================================
	//===========================================================================
	/**
	 * Ajoute un role pour l'utilisateur courant.
	 * Le role doit avoir ete prealablement enregistre.
	 *
	 * @param role Role e ajouter.
	 */
	public final UserSession addRole(final Role role) {
		Assertion.checkNotNull(role);
		// ----------------------------------------------------------------------
		roles.add(new DefinitionReference<>(role));
		return this;
	}

	/**
	 * Retourne la liste des roles de securite pour l'utilisateur.
	 *
	 * @return Set des roles.
	 */
	public final Set<Role> getRoles() {
		final Set<Role> roleSet = new HashSet<>();
		for (final DefinitionReference<Role> roleReference : roles) {
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
		// ----------------------------------------------------------------------
		return roles.contains(new DefinitionReference<>(role));
	}

	/**
	 * Retrait de tous les roles possedes par l'utilisateur. 
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public final void clearRoles() {
		roles.clear();
	}

	//===========================================================================
	//=======================GESTION DES AUTHENTIFICATIONS=======================
	//===========================================================================

	/**
	 * @return UUID Indentifiant unique de cette connexion.
	 */
	public final UUID getSessionUUID() {
		return sessionUUID;
	}

	/**
	 * Indique si l'utilisateur est authentifie.
	 * L'authentification est act�e par l'appel de la m�thode <code>authenticate()</code>
	 *
	 * @return boolean Si l'utilisateur s'est authentifi�.
	 */
	public final boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * M�thode permettant d'indiquer que l'utilisateur est authentifi�.
	 */
	public final void authenticate() {
		authenticated = true;
	}

	/**
	 * Gestion multilingue.
	 * Local associ�e � l'utilisateur.
	 * @return Locale associ�e � l'utilisateur.
	 */
	public abstract Locale getLocale();

	/**
	 * Gestion de la s�curit�.
	 * @return Liste des cl�s de s�curit� et leur valeur.
	 */
	public Map<String, String> getSecurityKeys() {
		return Collections.emptyMap();
	}
}
