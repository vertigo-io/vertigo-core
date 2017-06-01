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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.account.authorization;

import java.util.List;

import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.metamodel.PermissionName;
import io.vertigo.core.component.Manager;
import io.vertigo.dynamo.domain.model.KeyConcept;

/**
 * Gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public interface AuthorizationManager extends Manager {

	/**
	 * User permissions accessor to test or add permissions.
	 * A UserSession must exists.
	 * @return UserPermissions
	 */
	UserPermissions obtainUserPermissions();

	/**
	 * Contrôle d'accès basé sur les permissions.
	 * Indique si l'utilisateur dispose de la permission nécessaire.
	 *
	 * @param permissionName permission. (non null)
	 * @return Si les droits de l'utilisateur lui permettent un accès.
	 */
	boolean hasPermission(PermissionName permissionName);

	/**
	 * Indique si l'utilisateur courant a la permission d'effectuer l'operation
	 * donnee sur la ressource donnee.
	 *
	 * @param keyConcept la ressource
	 * @param operation l'operation
	 * @return true si l'utilisateur courant a la permission d'effectuer l'operation donnée sur la ressource donnee
	 * @param <K> Type du keyConcept
	 */
	<K extends KeyConcept> boolean isAuthorized(final K keyConcept, OperationName<K> operation);

	/**
	 * Indique si l'utilisateur courant a la permission d'effectuer l'operation
	 * donnee sur la ressource donnee.
	 *
	 * @param keyConcept la ressource
	 * @param operation l'operation
	 * @return true si l'utilisateur courant a la permission d'effectuer l'operation donnée sur la ressource donnee
	 * @param <K> Type du keyConcept
	 */
	<K extends KeyConcept> String getSearchSecurity(final K keyConcept, OperationName<K> operation);

	/**
	 * Retourne la liste des opérations autorisées sur le keyConcept.
	 *
	 * @param keyConcept objet sécurisé.
	 * @return liste d'opérations.
	 * @param <K> Type du keyConcept
	 */
	<K extends KeyConcept> List<String> getAuthorizedOperations(final K keyConcept);

}
