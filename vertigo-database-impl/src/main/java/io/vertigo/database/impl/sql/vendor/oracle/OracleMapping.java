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
package io.vertigo.database.impl.sql.vendor.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;

import io.vertigo.database.impl.sql.vendor.core.SqlMappingImpl;
import io.vertigo.database.sql.vendor.SqlMapping;

/**
 * This class implements the mapping for the Oracle database.
 *
 * @author pchretien
 */
final class OracleMapping implements SqlMapping {
	private final SqlMapping defaultSQLMapping = new SqlMappingImpl();

	/** {@inheritDoc} */
	@Override
	public <O> void setValueOnStatement(
			final java.sql.PreparedStatement statement,
			final int index,
			final Class<O> dataType,
			final O value) throws SQLException {

		defaultSQLMapping.setValueOnStatement(statement, index, dataType, value);
	}

	/** {@inheritDoc} */
	@Override
	public <O> O getValueForResultSet(
			final ResultSet resultSet,
			final int col,
			final Class<O> dataType) throws SQLException {
		return defaultSQLMapping.getValueForResultSet(resultSet, col, dataType);
	}
}
