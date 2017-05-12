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
package io.vertigo.dynamo.impl.database.statement;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.BeanUtil;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * Centralisation du peuplement des beans.
 * @author  pchretien
 */
final class SqlUtil {

	private SqlUtil() {
		//private constructor.
	}

	/**
	 * Création du résultat issu d'un resultSet.
	 * @param domain Domain résultat
	 * @param mapping Mapping SQL
	 * @param resultSet ResultSet comprenant résultat et Metadonnées permettant le cas échéant de créer dynamiquement un type dynamiquement.
	 * @return Résultat de la requête.
	 * @throws SQLException Exception SQL
	 */
	static SqlQueryResult buildResult(final Type dataType, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		Assertion.checkNotNull(dataType);
		Assertion.checkNotNull(mapping);
		Assertion.checkNotNull(resultSet);
		//-----
		if (isPrimitive(dataType)) {
			return retrievePrimitive(dataType, mapping, resultSet);
		}
		return retrieveData(dataType, mapping, resultSet);
	}

	private static boolean isDtObject(final Type dataType) {
		return ((Class) dataType).isAssignableFrom(DtObject.class);
	}

	private static SqlQueryResult retrievePrimitive(final Type dataType, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (resultSet.next()) {
			//We are excepting at most one object.
			//An exception is thrown if more than one object is found
			final Object value = mapping.getValueForResultSet(resultSet, 1, dataType);
			if (resultSet.next()) {
				throw createTooManyRowsException();
			}
			return new SqlQueryResult(value, 1);
		}
		//no data found
		return new SqlQueryResult(null, 0);
	}

	private static SqlQueryResult retrieveData(final Type dataType, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final Type retieveDataType;
		final boolean isObject = !dataType.getTypeName().startsWith("$");
		// case list
		if (isObject) {
			retieveDataType = dataType;
		} else {
			retieveDataType = ClassUtil.classForName(dataType.getTypeName().substring(1));
		}

		final Integer limit = isObject ? 1 : null;
		final DtList<DtObject> dtc = doRetrieveList(retieveDataType, mapping, resultSet, limit);
		if (isObject) {
			final DtObject dto = dtc.isEmpty() ? null : dtc.get(0);
			return new SqlQueryResult(dto, dtc.size());
		}
		return new SqlQueryResult(dtc, dtc.size());
	}

	private static DtList<DtObject> doRetrieveList(final Type dataType, final SqlMapping mapping, final ResultSet resultSet, final Integer limit) throws SQLException {
		final Field[] fields = findFields(dataType, resultSet.getMetaData());
		//Dans le cas d'une collection on retourne toujours qqChose
		//Si la requête ne retourne aucune ligne, on retourne une collection vide.
		final DtList<DtObject> dtc = new DtList<>(DtObjectUtil.findDtDefinition((Class) dataType));
		while (resultSet.next()) {
			if (limit != null && dtc.size() > limit) {
				throw createTooManyRowsException();
			}
			dtc.add(readDtObject(mapping, resultSet, dataType, fields));
		}
		return dtc;
	}

	private static DtObject readDtObject(final SqlMapping mapping, final ResultSet resultSet, final Type dataType, final Field[] fields) throws SQLException {
		final DtObject dto = DtObjectUtil.createDtObject(DtObjectUtil.findDtDefinition((Class) dataType));
		Object value;
		for (int i = 0; i < fields.length; i++) {
			value = mapping.getValueForResultSet(resultSet, i + 1, fields[i].getType());
			BeanUtil.setValue(dto, fields[i].getName(), value);
		}
		return dto;
	}

	/**
	 * Détermine les champs ramenés par un select.
	 * @param resultSetMetaData Metadonnées obtenues après exécution de la requête SQL.
	 * @return Tableau de codes de champ.
	 */
	private static Field[] findFields(final Type dataType, final ResultSetMetaData resultSetMetaData) throws SQLException {
		final Field[] fields = new Field[resultSetMetaData.getColumnCount()];
		String columnLabel;
		for (int i = 0; i < fields.length; i++) {
			columnLabel = resultSetMetaData.getColumnLabel(i + 1); //getColumnLabel permet de récupérer le nom adapté lors du select (avec un select truc as machin from xxx)
			// toUpperCase nécessaire pour postgreSQL et SQLServer
			try {
				fields[i] = ((Class) dataType).getDeclaredField(StringUtil.constToLowerCamelCase(columnLabel.toUpperCase(Locale.ENGLISH)));
			} catch (final Exception e) {
				throw WrappedException.wrap(e);
			}
		}
		return fields;
	}

	private static RuntimeException createTooManyRowsException() {
		return new IllegalStateException("load TooManyRows");
	}

	private static boolean isPrimitive(final Type dataType) {
		if (dataType instanceof Class) {
			final Class[] primiviteTypes = new Class[] { Integer.class, Double.class, Boolean.class, String.class, Date.class, BigDecimal.class, Long.class };
			return Stream.of(primiviteTypes)
					.anyMatch(primitiveClazz -> primitiveClazz.isAssignableFrom((Class) dataType));
		}
		return false;
	}

}
