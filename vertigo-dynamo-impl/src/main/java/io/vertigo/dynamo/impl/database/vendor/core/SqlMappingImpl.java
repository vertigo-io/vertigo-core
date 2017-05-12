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
package io.vertigo.dynamo.impl.database.vendor.core;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataStream;

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
	public Type getDataType(final int typeSQL) {
		final Type dataType;
		switch (typeSQL) {
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.INTEGER:
			case Types.NUMERIC:
				dataType = Integer.class;
				break;
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				dataType = String.class;
				break;
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				dataType = Date.class;
				break;
			case Types.BIGINT:
				dataType = Long.class;
				break;
			case Types.BOOLEAN:
			case Types.BIT:
				dataType = Boolean.class;
				break;
			case Types.DECIMAL:
				dataType = BigDecimal.class;
				break;
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.REAL:
				dataType = Double.class;
				break;
			case Types.BLOB:
				dataType = DataStream.class;
				break;
			default:
				throw new IllegalArgumentException("Type SQL non géré (" + typeSQL + ')');
		}
		return dataType;
	}

	/** {@inheritDoc} */
	@Override
	public int getSqlType(final Type dataType) {
		final Class clazz = (Class) dataType;
		if (Integer.class.isAssignableFrom(clazz)) {
			return Types.INTEGER;
		}
		if (Boolean.class.isAssignableFrom(clazz)) {
			return Types.BIT;
		}
		if (Long.class.isAssignableFrom(clazz)) {
			return Types.BIGINT;
		}
		if (Double.class.isAssignableFrom(clazz)) {
			return Types.DOUBLE;
		}
		if (BigDecimal.class.isAssignableFrom(clazz)) {
			return Types.DECIMAL;
		}
		if (String.class.isAssignableFrom(clazz)) {
			return Types.VARCHAR;
		}
		if (Date.class.isAssignableFrom(clazz)) {
			return Types.TIMESTAMP;
		}
		if (DataStream.class.isAssignableFrom(clazz)) {
			return Types.BLOB;
		}
		throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
	}

	/** {@inheritDoc} */
	@Override
	public void setValueOnStatement(final java.sql.PreparedStatement statement, final int index, final Type dataType, final Object value) throws SQLException {
		final Class clazz = (Class) dataType;
		if (value == null) {
			final int typeSQL = getSqlType(dataType);
			statement.setNull(index, typeSQL);
		} else {
			if (Integer.class.isAssignableFrom(clazz)) {
				statement.setInt(index, (Integer) value);
			} else if (Long.class.isAssignableFrom(clazz)) {
				statement.setLong(index, (Long) value);
			} else if (Boolean.class.isAssignableFrom(clazz)) {
				final int intValue = Boolean.TRUE.equals(value) ? 1 : 0;
				statement.setInt(index, intValue);
			} else if (Double.class.isAssignableFrom(clazz)) {
				statement.setDouble(index, (Double) value);
			} else if (BigDecimal.class.isAssignableFrom(clazz)) {
				statement.setBigDecimal(index, (BigDecimal) value);
			} else if (String.class.isAssignableFrom(clazz)) {
				statement.setString(index, (String) value);
			} else if (Date.class.isAssignableFrom(clazz)) {
				if (value instanceof Timestamp) {
					statement.setTimestamp(index, (Timestamp) value);
				} else {
					final Timestamp ts = new Timestamp(((java.util.Date) value).getTime());
					statement.setTimestamp(index, ts);
				}
			} else if (DataStream.class.isAssignableFrom(clazz)) {
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
	public Object getValueForCallableStatement(final CallableStatement callableStatement, final int index, final Type dataType) throws SQLException {
		final Class clazz = (Class) dataType;
		Object o;
		if (Integer.class.isAssignableFrom(clazz)) {
			o = callableStatement.getInt(index);
		} else if (Long.class.isAssignableFrom(clazz)) {
			o = callableStatement.getLong(index);
		} else if (Boolean.class.isAssignableFrom(clazz)) {
			o = callableStatement.getBoolean(index);
		} else if (Double.class.isAssignableFrom(clazz)) {
			o = callableStatement.getDouble(index);
		} else if (BigDecimal.class.isAssignableFrom(clazz)) {
			o = callableStatement.getBigDecimal(index);
		} else if (String.class.isAssignableFrom(clazz)) {
			o = callableStatement.getString(index);
		} else if (Date.class.isAssignableFrom(clazz)) {
			//Pour avoir une date avec les heures (Sens Java !)
			//il faut récupérer le timeStamp
			//Puis le transformer en java.util.Date (Date+heure)
			final Timestamp timestamp = callableStatement.getTimestamp(index); //peut etre null !!
			if (timestamp != null) {
				o = new java.util.Date(timestamp.getTime());
			} else {
				o = null;
			}
		} else {
			throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
		}
		if (callableStatement.wasNull()) {
			o = null;
		}
		return o;
	}

	/** {@inheritDoc} */
	@Override
	public Object getValueForResultSet(final ResultSet resultSet, final int col, final Type dataType) throws SQLException {
		final Class clazz = (Class) dataType;
		final Object value;

		if (String.class.isAssignableFrom(clazz)) {
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
		} else if (Integer.class.isAssignableFrom(clazz)) {
			final int vi = resultSet.getInt(col);
			value = resultSet.wasNull() ? null : vi;
		} else if (Long.class.isAssignableFrom(clazz)) {
			final long vl = resultSet.getLong(col);
			value = resultSet.wasNull() ? null : vl;
		} else if (Boolean.class.isAssignableFrom(clazz)) {
			final int vb = resultSet.getInt(col);
			value = resultSet.wasNull() ? null : vb != 0 ? Boolean.TRUE : Boolean.FALSE;
		} else if (Double.class.isAssignableFrom(clazz)) {
			final double vd = resultSet.getDouble(col);
			value = resultSet.wasNull() ? null : vd;
		} else if (BigDecimal.class.isAssignableFrom(clazz)) {
			//Si la valeur est null rs renvoie bien null
			value = resultSet.getBigDecimal(col);
		} else if (Date.class.isAssignableFrom(clazz)) {
			//Si la valeur est null rs renvoie bien null
			final Timestamp timestamp = resultSet.getTimestamp(col);

			//Pour avoir une date avec les heures (Sens Java !)
			//il faut récupérer le timeStamp
			//Puis le transformer en java.util.Date (Date+heure)
			value = timestamp == null ? null : new java.util.Date(timestamp.getTime());
		} else if (DataStream.class.isAssignableFrom(clazz)) {
			value = SqlDataStreamMappingUtil.getDataStream(resultSet, col);
		} else {
			throw new IllegalArgumentException(TYPE_UNSUPPORTED + dataType);
		}

		return value;
	}

}
