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

/**
 * The result of a sql query
 * @author pchretien
 */
public final class SqlQueryResult {
	private final Object value;
	private final int sqlRowCount;

	/**
	 * Constructor.
	 * @param value the result of the query (a simple object or a list)
	 * @param sqlRowCount the number of rows read in the database to execute the query
	 */
	public SqlQueryResult(final Object value, final int sqlRowCount) {
		this.value = value; //may be null
		this.sqlRowCount = sqlRowCount;
	}

	/**
	 * @return the result of the query (a simple object or a list)
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the number of rows read in the database to execute the query
	 */
	public int getSQLRowCount() {
		return sqlRowCount;
	}
}
