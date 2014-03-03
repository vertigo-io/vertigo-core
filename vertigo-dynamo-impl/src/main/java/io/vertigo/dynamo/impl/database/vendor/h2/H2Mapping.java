package io.vertigo.dynamo.impl.database.vendor.h2;

import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Implémentation spécifique à H2.
 * 
 * @author jmainaud
 * @version $Id: H2Mapping.java,v 1.2 2014/01/20 17:46:11 pchretien Exp $
 */
final class H2Mapping implements SQLMapping {
	private final SQLMapping defaultSQLMapping = new SQLMappingImpl();

	/** {@inheritDoc} */
	public int getTypeSQL(final KDataType dataType) {
		if (dataType == KDataType.Boolean) {
			return Types.BOOLEAN;
		}
		return defaultSQLMapping.getTypeSQL(dataType);
	}

	/** {@inheritDoc} */
	public void setValueOnStatement(final java.sql.PreparedStatement statement, final int index, final KDataType dataType, final Object value) throws SQLException {
		if (dataType == KDataType.Boolean) {
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
	public Object getValueForCallableStatement(final CallableStatement callableStatement, final int index, final KDataType dataType) throws SQLException {
		if (dataType == KDataType.Boolean) {
			final boolean vb = callableStatement.getBoolean(index);
			return callableStatement.wasNull() ? null : vb;
		}
		return defaultSQLMapping.getValueForCallableStatement(callableStatement, index, dataType);
	}

	/** {@inheritDoc} */
	public Object getValueForResultSet(final ResultSet rs, final int col, final KDataType dataType) throws SQLException {
		if (dataType == KDataType.Boolean) {
			final boolean vb = rs.getBoolean(col);
			return rs.wasNull() ? null : vb;
		}
		return defaultSQLMapping.getValueForResultSet(rs, col, dataType);
	}

	/** {@inheritDoc} */
	public KDataType getDataType(final int typeSQL) {
		return defaultSQLMapping.getDataType(typeSQL);
	}
}
