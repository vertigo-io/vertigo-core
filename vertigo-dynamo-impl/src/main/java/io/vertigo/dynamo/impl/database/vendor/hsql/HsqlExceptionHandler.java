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
package io.vertigo.dynamo.impl.database.vendor.hsql;

import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.impl.database.vendor.core.AbstractSqlExceptionHandler;

import java.sql.SQLException;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour HSQL.
 * @author dchallas
 */
final class HsqlExceptionHandler extends AbstractSqlExceptionHandler {
	/**
	 * Constructeur.
	 */
	HsqlExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	public void handleSQLException(final SQLException sqle, final SqlPreparedStatement statement) {
		// Message d'erreur par défaut
		handleOtherSQLException(sqle, statement);
		// voir les codes dans org.hsqldb.Trace
	}

	@Override
	protected String extractConstraintName(final String msg) {
		// TODO Auto-generated method stub
		// voir les codes dans org.hsqldb.Trace
		return msg;
	}

}
