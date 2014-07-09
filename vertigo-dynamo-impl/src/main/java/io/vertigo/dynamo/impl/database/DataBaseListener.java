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
package io.vertigo.dynamo.impl.database;

import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.impl.database.statement.StatementStats;

/**
* Interface de réception des  événements produits par l'exécution des taches SQL.
*
* @author pchretien
*/
public interface DataBaseListener {
	/**
	 * Enregistre le début d'exécution d'un PreparedStatement.
	 * @param preparedStatement Statement
	 */
	void onPreparedStatementStart(KPreparedStatement preparedStatement);

	/**
	 * Enregistre la fin d'une exécution de PreparedStatement avec le temps d'exécution en ms et son statut (OK/KO).
	 * @param statementStats Informations sur l'éxécution
	 */
	void onPreparedStatementFinish(StatementStats statementStats);
}
