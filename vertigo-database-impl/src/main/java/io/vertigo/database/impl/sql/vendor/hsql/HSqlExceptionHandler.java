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
package io.vertigo.database.impl.sql.vendor.hsql;

import java.sql.SQLException;

import io.vertigo.database.impl.sql.vendor.core.AbstractSqlExceptionHandler;

/**
 * Handler des exceptions SQL qui peuvent survenir dans une tache.
 * Cette implémentation est adaptée pour HSQL.
 * @author dchallas
 */
final class HSqlExceptionHandler extends AbstractSqlExceptionHandler {
	/**
	 * Constructor.
	 */
	HSqlExceptionHandler() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public RuntimeException handleSQLException(final SQLException sqle, final String statementInfos) {
		// default message
		return handleOtherSQLException(sqle, statementInfos);
		// sea  sql codes  at org.hsqldb.Trace
	}

	@Override
	protected String extractConstraintName(final String msg) {
		// voir les codes dans org.hsqldb.Trace
		return msg;
	}

}
