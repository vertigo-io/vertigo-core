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
package io.vertigo.database.impl.sql.vendor.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import io.vertigo.database.sql.vendor.SqlMapping;
import io.vertigo.lang.DataStream;

/**
 * Implémentation par défaut du mapping à la BDD.
 * Cette implmentation peut être redéfinie partiellement ou totalement.
 *
 * @author pchretien
 */
public final class SqlMappingImpl implements SqlMapping {
	private static final String TYPE_UNSUPPORTED = "Type unsupported : ";

	/** {@inheritDoc} */
	@Override
	public int getSqlType(final Class dataType) {
		if (Integer.class.isAssignableFrom(dataType)) {
			return Types.INTEGER;
		}
		if (Boolean.class.isAssignableFrom(dataType)) {
			return Types.BIT;
		}
		if (Long.class.isAssignableFrom(dataType)) {
			return Types.BIGINT;
		}
		if (Double.class.isAssignableFrom(dataType)) {
			return Types.DOUBLE;
		}
		if (BigDecimal.class.isAssignableFrom(dataType)) {
			return Types.DECIMAL;
		}
		if (String.class.isAssignableFrom(dataType)) {
			return Types.VARCHAR;
		}
		if (Date.class.isAssignableFrom(dataType)) {
			return Types.TIMESTAMP;
		}
		if (DataStream.class.isAssignableFrom(dataType)) {
			return Types.BLOB;
		}
		throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
	}

	/** {@inheritDoc} */
	@Override
	public <O> void setValueOnStatement(
			final java.sql.PreparedStatement statement,
			final int index,
			final Class<O> dataType,
			final O value) throws SQLException {
		if (value == null) {
			final int typeSQL = getSqlType(dataType);
			statement.setNull(index, typeSQL);
		} else {
			if (Integer.class.isAssignableFrom(dataType)) {
				statement.setInt(index, (Integer) value);
			} else if (Long.class.isAssignableFrom(dataType)) {
				statement.setLong(index, (Long) value);
			} else if (Boolean.class.isAssignableFrom(dataType)) {
				final int intValue = Boolean.TRUE.equals(value) ? 1 : 0;
				statement.setInt(index, intValue);
			} else if (Double.class.isAssignableFrom(dataType)) {
				statement.setDouble(index, (Double) value);
			} else if (BigDecimal.class.isAssignableFrom(dataType)) {
				statement.setBigDecimal(index, (BigDecimal) value);
			} else if (String.class.isAssignableFrom(dataType)) {
				statement.setString(index, (String) value);
			} else if (Date.class.isAssignableFrom(dataType)) {
				if (value instanceof Timestamp) {
					statement.setTimestamp(index, (Timestamp) value);
				} else {
					final Timestamp ts = new Timestamp(((java.util.Date) value).getTime());
					statement.setTimestamp(index, ts);
				}
			} else if (DataStream.class.isAssignableFrom(dataType)) {
				try {
					final DataStream dataStream = (DataStream) value;
					//attention le setBinaryStream avec une longueur de fichier en long N'EST PAS implémentée dans de nombreux drivers !!
					statement.setBinaryStream(index, dataStream.createInputStream(), (int) dataStream.getLength());
				} catch (final IOException e) {
					throw new SQLException("Erreur d'ecriture du flux");
				}
			} else {
				throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
			}

		}
	}

	/** {@inheritDoc} */
	@Override
	public <O> O getValueForResultSet(
			final ResultSet resultSet,
			final int col,
			final Class<O> dataType) throws SQLException {
		final Object value;

		if (String.class.isAssignableFrom(dataType)) {
			if (resultSet.getMetaData().getColumnType(col) == Types.CLOB) {
				final Clob clob = resultSet.getClob(col);
				//Si la valeur est null rs renvoie bien null
				if (clob != null) {
					final Long len = clob.length();
					value = clob.getSubString(1L, len.intValue());
				} else {
					value = null;
				}
			} else {
				//Si la valeur est null rs renvoie bien null
				value = resultSet.getString(col);
			}
		} else if (Integer.class.isAssignableFrom(dataType)) {
			final int vi = resultSet.getInt(col);
			value = resultSet.wasNull() ? null : vi;
		} else if (Long.class.isAssignableFrom(dataType)) {
			final long vl = resultSet.getLong(col);
			value = resultSet.wasNull() ? null : vl;
		} else if (Boolean.class.isAssignableFrom(dataType)) {
			final int vb = resultSet.getInt(col);
			value = resultSet.wasNull() ? null : vb != 0 ? Boolean.TRUE : Boolean.FALSE;
		} else if (Double.class.isAssignableFrom(dataType)) {
			final double vd = resultSet.getDouble(col);
			value = resultSet.wasNull() ? null : vd;
		} else if (BigDecimal.class.isAssignableFrom(dataType)) {
			//Si la valeur est null rs renvoie bien null
			value = resultSet.getBigDecimal(col);
		} else if (Date.class.isAssignableFrom(dataType)) {
			//Si la valeur est null rs renvoie bien null
			final Timestamp timestamp = resultSet.getTimestamp(col);

			//Pour avoir une date avec les heures (Sens Java !)
			//il faut récupérer le timeStamp
			//Puis le transformer en java.util.Date (Date+heure)
			value = timestamp == null ? null : new java.util.Date(timestamp.getTime());
		} else if (DataStream.class.isAssignableFrom(dataType)) {
			value = SqlDataStreamMappingUtil.getDataStream(resultSet, col);
		} else {
			throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
		}

		return dataType.cast(value);
	}
}
