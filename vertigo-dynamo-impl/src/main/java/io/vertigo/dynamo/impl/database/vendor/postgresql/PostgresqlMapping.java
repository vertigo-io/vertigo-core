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
package io.vertigo.dynamo.impl.database.vendor.postgresql;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.impl.database.vendor.core.SqlMappingImpl;

/**
 * Implémentation spécifique à Postgresql.
 *
 * @author pchretien
 */
final class PostgresqlMapping implements SqlMapping {
	private final SqlMapping defaultSQLMapping = new SqlMappingImpl();

	/** {@inheritDoc} */
	@Override
	public int getSqlType(final Type dataType) {
		if (Boolean.class.isAssignableFrom((Class) dataType)) {
			return Types.BOOLEAN;
		}
		return defaultSQLMapping.getSqlType(dataType);
	}

	/** {@inheritDoc} */
	@Override
	public void setValueOnStatement(final java.sql.PreparedStatement statement, final int index, final Type dataType, final Object value) throws SQLException {
		if (value == null) {
			defaultSQLMapping.setValueOnStatement(statement, index, dataType, null /*value*/);
		} else if (Boolean.class.isAssignableFrom((Class) dataType)) {
			statement.setBoolean(index, Boolean.TRUE.equals(value));
		} else {
			defaultSQLMapping.setValueOnStatement(statement, index, dataType, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueForCallableStatement(final CallableStatement callableStatement, final int index, final Type dataType) throws SQLException {
		return defaultSQLMapping.getValueForCallableStatement(callableStatement, index, dataType);
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueForResultSet(final ResultSet resultSet, final int col, final Type dataType) throws SQLException {
		if (Boolean.class.isAssignableFrom((Class) dataType)) {
			final boolean vb = resultSet.getBoolean(col);
			return resultSet.wasNull() ? null : vb;
		}
		return defaultSQLMapping.getValueForResultSet(resultSet, col, dataType);
	}

	/** {@inheritDoc} */
	@Override
	public Type getDataType(final int typeSQL) {
		return defaultSQLMapping.getDataType(typeSQL);
	}
}
