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

import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Option;
import io.vertigo.persona.security.metamodel.Role;

import java.util.Set;

/**
 * Gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 * @version $Id: KSecurityManager.java,v 1.3 2013/10/23 11:53:43 pchretien Exp $
 */
public interface KSecurityManager extends Manager {
	//-------------------------------------------------------------------------
	//--------------Gestion de l'utilisateur (porteur des droits) -------------
	//-------------------------------------------------------------------------
	/**
	 * Accroche l'utilisateur au thread courant.
	 * S'effectue dans la servlet. Ne pas utiliser directement.
	 * @param userSession Session utilisateur (not null)
	 */
	void startCurrentUserSession(final UserSession userSession);

	/**
	 * Reinitialise la session courante.
	 */
	void stopCurrentUserSession();

	/**
	 * Recuperation de la session utilisateur courante.
	 * @param <U> Session utilisateur
	 * @return Session utilisateur courante.
	 */
	<U extends UserSession> Option<U> getCurrentUserSession();

	/**
	 * Creation de nouveaux utilisateurs. 
	 * @param <U> Type de l'utilisateur 
	 * @return Nouvel utilisateur
	 */
	<U extends UserSession> U createUserSession();

	/**
	 * Contrôle d'accès basé sur les rôles.
	 * 
	 * L'utilisateur dispose-t-il des droits nécessaires.
	 * <br/>
	 * <ul>
	 * <li>Si la liste des rôles autorisés est vide, on considère que l'objet n'est pas soumis à autorisation et donc l'accès est accordé.</li>
	 * <li>Si la liste contient au moins un élément alors l'objet est sécurisé et il est nécessaire que
	 * l'utilisateur dispose d'au moins un des rôles autorisés pour que l'accès soit accordé.</li>
	 * </ul>
	 *
	 * La fonction d'accès autorise la session utilisateur <code>null</code> : il faut alors que la liste des droits soit vide.
	 *
	 * @param userSession Session utilisateur. (non null)
	 * @param authorizedRoleSet Set des roles autorisés. (non null)
	 *
	 * @return Si les droits de l'utilisateur lui permettent un accès.
	 */
	boolean hasRole(UserSession userSession, Set<Role> authorizedRoleSet);

	/**
	 * Controle d'acces base sur les permissions.
	 * 
	 * Indique si l'utilisateur courant a la permission d'effectuer l'operation
	 * donnee sur la ressource donnee.
	 * 
	 * @param resource la ressource
	 * @param operation l'operation
	 * @return true si l'utilisateur courant a la permission d'effectuer l'operation
	 * donnée sur la ressource donnee
	 */
	boolean isAuthorized(String resource, String operation);

	/**
	 * Contrôle d'accès basé sur les permissions.
	 * 
	 * Indique si l'utilisateur courant a la permission d'effectuer l'opération
	 * donnée sur la ressource donnée.
	 * @param resourceType Type de la resource
	 * @param resource la ressource
	 * @param operation l'opération
	 * @return true si l'utilisateur courant a la permission d'effectuer l'opération
	 * donnée sur la ressource donnée
	 */
	boolean isAuthorized(String resourceType, Object resource, String operation);

	/**
	 * Enregistre une ResourceNameFactory spécifique pour un type donnée.
	 * @param resourceType Type de la resource
	 * @param resourceNameFactory ResourceNameFactory spécifique
	 */
	void registerResourceNameFactory(final String resourceType, final ResourceNameFactory resourceNameFactory);
}
