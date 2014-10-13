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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;

/**
* Interface de statistiques pour le suivi des traitements SQL.
*
* @author npiedeloup
*/
public interface SqlStatementStats {
	/**
	 * @return preparedStatement Statement
	 */
	SqlPreparedStatement getPreparedStatement();

	/**
	 * @return elapsedTime Temps d'exécution en ms
	 */
	long getElapsedTime();

	/**
	 * @return Nombre de lignes affectées (update, insert, delete), null si sans objet
	 */
	Long getNbModifiedRow();

	/**
	 * @return Nombre de lignes récupérées (select), null si sans objet
	 */
	Long getNbSelectedRow();

	/**
	 * @return success Si l'exécution a réussi
	 */
	boolean isSuccess();
}
