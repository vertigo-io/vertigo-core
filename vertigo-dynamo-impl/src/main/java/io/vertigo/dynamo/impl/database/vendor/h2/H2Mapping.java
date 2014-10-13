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
package io.vertigo.dynamo.impl.database.vendor.h2;

import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.vendor.core.SqlMappingImpl;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Implémentation spécifique à H2.
 * 
 * @author jmainaud
 */
final class H2Mapping implements SqlMapping {
	private final SqlMapping defaultSQLMapping = new SqlMappingImpl();

	/** {@inheritDoc} */
	public int getSqlType(final DataType dataType) {
		if (dataType == DataType.Boolean) {
			return Types.BOOLEAN;
		}
		return defaultSQLMapping.getSqlType(dataType);
	}

	/** {@inheritDoc} */
	public void setValueOnStatement(final java.sql.PreparedStatement statement, final int index, final DataType dataType, final Object value) throws SQLException {
		if (dataType == DataType.Boolean) {
			if (value == null) {
				statement.setNull(index, Types.BOOLEAN);
			} else {
				statement.setBoolean(index, Boolean.TRUE.equals(value));
			}
		} else {
			defaultSQLMapping.setValueOnStatement(statement, index, dataType, value);
		}
	}

	/** {@inheritDoc} */
	public Object getValueForCallableStatement(final CallableStatement callableStatement, final int index, final DataType dataType) throws SQLException {
		if (dataType == DataType.Boolean) {
			final boolean vb = callableStatement.getBoolean(index);
			return callableStatement.wasNull() ? null : vb;
		}
		return defaultSQLMapping.getValueForCallableStatement(callableStatement, index, dataType);
	}

	/** {@inheritDoc} */
	public Object getValueForResultSet(final ResultSet rs, final int col, final DataType dataType) throws SQLException {
		if (dataType == DataType.Boolean) {
			final boolean vb = rs.getBoolean(col);
			return rs.wasNull() ? null : vb;
		}
		return defaultSQLMapping.getValueForResultSet(rs, col, dataType);
	}

	/** {@inheritDoc} */
	public DataType getDataType(final int typeSQL) {
		return defaultSQLMapping.getDataType(typeSQL);
	}
}
