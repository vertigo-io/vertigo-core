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
package io.vertigo.dynamo.plugins.database.connection.hibernate;

import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.impl.database.vendor.core.SqlDataStreamMappingUtil;
import io.vertigo.util.BeanUtil;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

/**
 * Custom hibernate UserType for DataStream.
 * DataStream map to only one Blob column.
 * @author npiedeloup
 */
public final class DataStreamType implements UserType {

	private static int[] SQL_TYPES = new int[] { Types.BLOB };

	/** {@inheritDoc} */
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	/** {@inheritDoc} */
	public Class returnedClass() {
		return DataStream.class;
	}

	/** {@inheritDoc} */
	public boolean equals(final Object x, final Object y) {
		return BeanUtil.equals(x, y);
	}

	/** {@inheritDoc} */
	public int hashCode(final Object x) throws HibernateException {
		if (x != null) {
			return x.hashCode();
		}
		return 0;
	}

	/** {@inheritDoc} */
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException, SQLException {
		//Cf io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl
		final String columnName = names[0];
		final int index = rs.findColumn(columnName);
		final DataStream value = SqlDataStreamMappingUtil.getDataStream(rs, index);
		return value;
	}

	/** {@inheritDoc} */
	public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			statement.setNull(index, sqlTypes()[0]);
		} else {
			//Cf io.vertigo.dynamo.impl.database.vendor.core.SQLMappingImpl
			try {
				final DataStream dataStream = (DataStream) value;
				statement.setBinaryStream(index, dataStream.createInputStream(), (int) dataStream.getLength()); //attention le setBinaryStream avec une longueur de fichier en long N'EST PAS implémentée dans de nombreux drivers !!
			} catch (final IOException e) {
				final SQLException sqlException = new SQLException("Erreur d'ecriture du flux");
				sqlException.initCause(e);
				throw sqlException;
			}
		}
	}

	/** {@inheritDoc} */
	public boolean isMutable() {
		return false;
	}

	/** {@inheritDoc} */
	public Object assemble(final Serializable cached, final Object owner) {
		return cached;
	}

	/** {@inheritDoc} */
	public Object deepCopy(final Object value) {
		return value;
	}

	/** {@inheritDoc} */
	public Serializable disassemble(final Object value) {
		return (Serializable) value;
	}

	/** {@inheritDoc} */
	public Object replace(final Object original, final Object target, final Object owner) {
		return original;
	}

}
