/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.vendor.oracle;

import java.sql.SQLException;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour Oracle.
 *
 * @author npiedeloup
 */
final class OracleExceptionHandler extends AbstractSqlExceptionHandler {
	/**
	 * Constructeur.
	 */
	OracleExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void handleSQLException(final SQLException sqle, final SqlPreparedStatement statement) {
		final int errCode = sqle.getErrorCode();
		if (errCode >= 20000 && errCode < 30000) {
			// Erreur utilisateur
			handleUserSQLException(sqle);
		} else if (errCode == 1438 || errCode == 12899) {
			// Valeur trop grande pour ce champs
			handleTooLargeValueSqlException(sqle);
		} else if (errCode == 2292) {
			// Violation de contrainte d'intégrité référentielle
			handleForeignConstraintSQLException(sqle);
		} else if (errCode == 1) {
			// Violation de contrainte d'unicité
			handleUniqueConstraintSQLException(sqle);
		} else {
			// Message d'erreur par défaut
			handleOtherSQLException(sqle, statement);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected String extractConstraintName(final String msg) {
		final int i1 = msg.indexOf('.', msg.indexOf('('));
		final int i2 = msg.indexOf(')', i1);
		if (i1 > -1 && i2 > -1) {
			return msg.substring(i1 + 1, i2);
		}
		return null;
	}
}
