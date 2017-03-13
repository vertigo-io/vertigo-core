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
package io.vertigo.dynamo.transaction;

/**
 * Ressource participant à une transaction.
 * Cette ressource est par exemple :
 * - une connexion à une BDD Oracle, Sybase, MySQL....
 * - un mailer
 * - un fileSystem
 * - un objet java
 *
 * @author  pchretien
 */
public interface VTransactionResource {
	/**
	 * Valide la ressource.
	 * @throws Exception Si impossible.
	 */
	void commit() throws Exception;

	/**
	 * Annule la ressource.
	 * @throws Exception Si impossible.
	 */
	void rollback() throws Exception;

	/**
	 * Libère la ressource.
	 * Appelée systématiquement après un commit ou un rollback.
	 * @throws Exception Si impossible.
	 */
	void release() throws Exception;
}
