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
package io.vertigo.database.impl.sql.statement;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import io.vertigo.database.sql.vendor.SqlMapping;
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
	static <O> List<O> buildResult(
			final Class<O> dataType,
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Integer limit) throws SQLException {
		Assertion.checkNotNull(dataType);
		Assertion.checkNotNull(mapping);
		Assertion.checkNotNull(resultSet);
		//-----
		return retrieveData(dataType, mapping, resultSet, limit);
	}

	private static <O> List<O> retrieveData(
			final Class<O> dataType,
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Integer limit) throws SQLException {
		final boolean isPrimitive = isPrimitive(dataType);

		final MyField[] fields = isPrimitive ? null : findFields(dataType, resultSet.getMetaData());
		//Dans le cas d'une collection on retourne toujours qqChose
		//Si la requête ne retourne aucune ligne, on retourne une collection vide.
		final List<O> list = new ArrayList<>();
		while (resultSet.next()) {
			if (limit != null && list.size() > limit) {
				throw createTooManyRowsException();
			}
			if (isPrimitive) {
				list.add(readPrimitive(mapping, resultSet, dataType));
			} else {
				list.add(readRow(mapping, resultSet, dataType, fields));
			}
		}
		return list;
	}

	private static <O> O readPrimitive(
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Class<O> dataType) throws SQLException {
		return mapping.getValueForResultSet(resultSet, 1, dataType);
	}

	private static <O> O readRow(
			final SqlMapping mapping,
			final ResultSet resultSet,
			final Class<O> dataType,
			final MyField[] fields) throws SQLException {
		final O bean = ClassUtil.newInstance(dataType);
		Object value;
		for (int i = 0; i < fields.length; i++) {
			value = mapping.getValueForResultSet(resultSet, i + 1, fields[i].type);
			fields[i].setValue(bean, value);
		}
		return bean;
	}

	/**
	 * Détermine les champs ramenés par un select.
	 * @param resultSetMetaData Metadonnées obtenues après exécution de la requête SQL.
	 * @return Tableau de codes de champ.
	 */
	private static MyField[] findFields(final Class dataType, final ResultSetMetaData resultSetMetaData) throws SQLException {
		final MyField[] fields = new MyField[resultSetMetaData.getColumnCount()];
		String columnLabel;
		for (int i = 0; i < fields.length; i++) {
			columnLabel = resultSetMetaData.getColumnLabel(i + 1); //getColumnLabel permet de récupérer le nom adapté lors du select (avec un select truc as machin from xxx)
			// toUpperCase nécessaire pour postgreSQL et SQLServer
			final String expectedFieldName = StringUtil.constToLowerCamelCase(columnLabel.toUpperCase(Locale.ENGLISH));
			try {
				final Method getter = dataType.getDeclaredMethod("get" + StringUtil.first2UpperCase(expectedFieldName));
				fields[i] = new MyField(expectedFieldName, getter.getReturnType());
			} catch (final Exception e) {
				throw WrappedException.wrap(e);
			}
		}
		return fields;
	}

	private static class MyField {
		protected final String name;
		protected final Class<?> type;

		MyField(final String name, final Class<?> type) {
			this.name = name;
			this.type = type;
		}

		void setValue(final Object bean, final Object value) {
			BeanUtil.setValue(bean, name, value);
		}

	}

	private static RuntimeException createTooManyRowsException() {
		return new IllegalStateException("load TooManyRows");
	}

	private static boolean isPrimitive(final Class dataType) {
		return Stream.of(
				Integer.class,
				Double.class,
				Boolean.class,
				String.class,
				Date.class,
				BigDecimal.class,
				Long.class)
				.anyMatch(primitiveClazz -> primitiveClazz.isAssignableFrom(dataType));
	}

}
