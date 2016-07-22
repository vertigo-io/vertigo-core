/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.statementhandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

/**
 * Centralisation du peuplement des beans.
 * @author  pchretien
 */
final class SqlRetrieveUtil {

	private SqlRetrieveUtil() {
		//Classe utilitaire, constructeir est privé.
	}

	static SqlQueryResult retrievePrimitive(final DataType dataType, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (resultSet.next()) {
			//On est dans le cas de récupération d'un objet, un objet a été trouvé
			//On vérifie qu'il y en a au plus un.
			final Object value = mapping.getValueForResultSet(resultSet, 1, dataType);
			if (resultSet.next()) {
				throw createTooManyRowsException();
			}
			return new SqlQueryResult(value, 1);
		}
		return new SqlQueryResult(null, 0);
	}

	static SqlQueryResult retrieveData(final SqlResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (resultMetaData.isDtObject()) {
			return retrieveEntity(resultMetaData, mapping, resultSet);
		}
		return retrieveEntityList(resultMetaData, mapping, resultSet);
	}

	private static SqlQueryResult retrieveEntity(final SqlResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final Entity entity = doRetrieveEntity(mapping, resultSet, resultMetaData);
		return new SqlQueryResult(entity, entity != null ? 1 : 0);
	}

	private static SqlQueryResult retrieveEntityList(final SqlResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final DtList<Entity> dtc = doRetrieveEntityList(mapping, resultSet, resultMetaData);
		return new SqlQueryResult(dtc, dtc.size());
	}

	private static DtList<Entity> doRetrieveEntityList(final SqlMapping mapping, final ResultSet resultSet, final SqlResultMetaData resultMetaData) throws SQLException {
		final DtField[] fields = findFields(resultMetaData, resultSet.getMetaData());

		Entity entity;
		//Dans le cas d'une collection on retourne toujours qqChose
		//Si la requête ne retourne aucune ligne, on retourne une collection vide.
		final DtList<Entity> dtc = new DtList<>(resultMetaData.getDtDefinition());
		while (resultSet.next()) {
			entity = resultMetaData.createEntity();
			readEntity(mapping, resultSet, entity, fields);
			dtc.add(entity);
		}
		return dtc;
	}

	private static Entity doRetrieveEntity(final SqlMapping mapping, final ResultSet resultSet, final SqlResultMetaData resultMetaData) throws SQLException {
		final DtField[] fields = findFields(resultMetaData, resultSet.getMetaData());

		if (resultSet.next()) {
			//On est dans le cas de récupération d'un objet, un objet a été trouvé
			//On vérifie qu'il y en a au plus un.
			final Entity entity = resultMetaData.createEntity();
			readEntity(mapping, resultSet, entity, fields);
			if (resultSet.next()) {
				throw createTooManyRowsException();
			}
			return entity;
		}
		//no result
		return null;
	}

	private static void readEntity(final SqlMapping mapping, final ResultSet resultSet, final Entity entity, final DtField[] fields) throws SQLException {
		Object value;
		for (int i = 0; i < fields.length; i++) {
			value = mapping.getValueForResultSet(resultSet, i + 1, fields[i].getDomain().getDataType());
			fields[i].getDataAccessor().setValue(entity, value);
		}
	}

	private static DtField[] findFields(final SqlResultMetaData resultMetaData, final ResultSetMetaData resultSetMetaData) throws SQLException {
		final String[] columnNames = getQueryColumnNames(resultSetMetaData);
		final DtField[] fields = new DtField[columnNames.length];
		for (int i = 0; i < fields.length; i++) {
			// toUpperCase nécessaire pour postgreSQL et SQLServer
			final DtField f = resultMetaData.getDtDefinition().getField(columnNames[i].toUpperCase());
			Assertion.checkNotNull(f);
			fields[i] = f;
		}
		return fields;
	}

	/**
	 * Détermine les champs ramenés par un select.
	 * @param resultSetMetaData Metadonnées obtenues après exécution de la requête SQL.
	 * @return Tableau de codes de champ.
	 */
	private static String[] getQueryColumnNames(final ResultSetMetaData resultSetMetaData) throws SQLException {
		Assertion.checkNotNull(resultSetMetaData);
		//-----
		final String[] res = new String[resultSetMetaData.getColumnCount()];
		for (int i = 0; i < res.length; i++) {
			res[i] = resultSetMetaData.getColumnLabel(i + 1); //getColumnLabel permet de récupérer le nom adapté lors du select (avec un select truc as machin from xxx)
		}
		return res;
	}

	private static RuntimeException createTooManyRowsException() {
		return new IllegalStateException("load TooManyRows");
	}
}
