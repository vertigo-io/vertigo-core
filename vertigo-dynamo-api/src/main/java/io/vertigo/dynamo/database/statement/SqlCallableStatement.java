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
package io.vertigo.dynamo.database.statement;

import java.sql.SQLException;

/**
 * Gestion des procédure stockées.
 *
 * @author pchretien
 */
public interface SqlCallableStatement extends SqlPreparedStatement {
	/**
	 * Getter générique.
	 * @param index Index du paramètre dans la requête SQL
	 * @throws SQLException Exception sql
	 * @return  Valeur du paramètre indexé
	 */
	Object getValue(final int index) throws SQLException;
}
