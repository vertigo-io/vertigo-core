/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
 * See the License for the specific language governing authorizations and
 * limitations under the License.
 */
package io.vertigo.account.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.account.authorization.metamodel.Authorization;
import io.vertigo.account.authorization.metamodel.AuthorizationName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * Authorizations d'un utilisateur.
 *
 * @author alauthier, pchretien, npiedeloup
 */
public final class UserAuthorizations implements Serializable {

	private static final long serialVersionUID = -7924146007592711123L;

	/**
	 * Set des authorizations global autorisees pour la session utilisateur.
	 */
	private final Map<String, DefinitionReference<Authorization>> authorizationRefs = new HashMap<>();

	/**
	 * Set des authorizations autorisees par entity pour la session utilisateur.
	 */
	private final Map<DefinitionReference<DtDefinition>, Set<DefinitionReference<Authorization>>> authorizationMapRefs = new HashMap<>();

	/**
	 * Set des roles autorises pour la session utilisateur.
	 * Pour compatibilité d'api.
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
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations addRole(final Role role) {
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
	public Set<Role> getRoles() {
		return roleRefs.stream()
				.map(roleRef -> roleRef.get())
				.collect(Collectors.toSet());
	}

	/**
	 * @param role Role
	 * @return Vrai si le role est present
	 */
	public boolean hasRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		return roleRefs.contains(new DefinitionReference<>(role));
	}

	/**
	 * Retrait de tous les roles possedes par l'utilisateur.
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public void clearRoles() {
		roleRefs.clear();
	}

	/**
	 * Ajoute une authorization pour l'utilisateur courant.
	 * La authorization doit avoir ete prealablement enregistree.
	 *
	 * @param authorization Authorization à ajouter.
	 * @return This UserAuthorizations
	 */
	public UserAuthorizations addAuthorization(final Authorization authorization) {
		Assertion.checkNotNull(authorization);
		//-----
		authorizationRefs.put(authorization.getName(), new DefinitionReference<>(authorization));
		if (authorization.getEntityDefinition().isPresent()) {
			authorizationMapRefs.computeIfAbsent(new DefinitionReference<>(authorization.getEntityDefinition().get()), key -> new HashSet<>())
					.add(new DefinitionReference<>(authorization));
			for (final Authorization grantedAuthorization : authorization.getGrants()) {
				if (!hasAuthorization(grantedAuthorization::getName)) { //On test pour ne pas créer de boucle
					addAuthorization(grantedAuthorization);
				}
			}
		}
		return this;
	}

	/**
	 * Retourne la liste des authorizations de securite d'une entity pour l'utilisateur.
	 * @param entityDefinition Entity definition
	 * @return Set des authorizations.
	 */
	public Set<Authorization> getEntityAuthorizations(final DtDefinition entityDefinition) {
		final Set<DefinitionReference<Authorization>> entityAuthorizationRefs = authorizationMapRefs.get(new DefinitionReference<>(entityDefinition));
		if (entityAuthorizationRefs != null) {
			return entityAuthorizationRefs.stream()
					.map(entityAuthorizationRef -> entityAuthorizationRef.get())
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	/**
	 * @param authorizationName Authorization
	 * @return Vrai si la authorization est presente
	 */
	public boolean hasAuthorization(final AuthorizationName authorizationName) {
		Assertion.checkNotNull(authorizationName);
		//-----
		return authorizationRefs.containsKey(authorizationName.name());
	}

	/**
	 * Retrait de toutes les authorizations possedes par l'utilisateur.
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public void clearAuthorizations() {
		authorizationRefs.clear();
		authorizationMapRefs.clear();
	}

	private final Map<String, List<Serializable>> mySecurityKeys = new HashMap<>();

	/**
	 * Gestion de la sécurité.
	 * @return Liste des clés de sécurité et leur valeur.
	 */
	public Map<String, List<Serializable>> getSecurityKeys() {
		return mySecurityKeys;
	}

	/**
	 * Add a security key part of his security perimeter.
	 * A security key can be multi-valued (then withSecurityKeys is call multiple times).
	 * Value should be an array if this securityKey is a tree (hierarchical) key.
	 *
	 * @param securityKey Name
	 * @param value Value
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations withSecurityKeys(final String securityKey, final Serializable value) {
		mySecurityKeys.computeIfAbsent(securityKey, v -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * Clear Security Keys.
	 * Use when user change it security perimeter.
	 * @return this UserAuthorizations
	 */
	public UserAuthorizations clearSecurityKeys() {
		mySecurityKeys.clear();
		return this;
	}
}
