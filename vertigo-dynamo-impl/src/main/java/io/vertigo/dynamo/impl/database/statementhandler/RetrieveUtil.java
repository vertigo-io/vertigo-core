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
package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.database.statement.QueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Centralisation du peuplement des beans.
 * @author  pchretien
 */
final class RetrieveUtil {

	private RetrieveUtil() {
		//Classe utilitaire, constructeir est privé.
	}

	static QueryResult retrievePrimitive(final DataType dataType, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (resultSet.next()) {
			//On est dans le cas de récupération d'un objet, un objet a été trouvé
			//On vérifie qu'il y en a au plus un.
			final Object value = mapping.getValueForResultSet(resultSet, 1, dataType);
			if (resultSet.next()) {
				throw createTooManyRowsException();
			}
			return new QueryResult(value, 1);
		}
		return new QueryResult(null, 0);
	}

	static QueryResult retrieveData(final ResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (resultMetaData.isDtObject()) {
			return retrieveDtObject(resultMetaData, mapping, resultSet);
		}
		return retrieveDtList(resultMetaData, mapping, resultSet);
	}

	private static QueryResult retrieveDtObject(final ResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final DtObject dto = doRetrieveDtObject(mapping, resultSet, resultMetaData);
		return new QueryResult(dto, dto != null ? 1 : 0);
	}

	private static QueryResult retrieveDtList(final ResultMetaData resultMetaData, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final DtList<DtObject> dtc = doRetrieveDtList(mapping, resultSet, resultMetaData);
		return new QueryResult(dtc, dtc.size());
	}

	private static DtList<DtObject> doRetrieveDtList(final SqlMapping mapping, final ResultSet resultSet, final ResultMetaData resultMetaData) throws SQLException {
		final DtField[] fields = findFields(resultMetaData, resultSet.getMetaData());

		DtObject dto;
		//Dans le cas d'une collection on retourne toujours qqChose
		//Si la requête ne retourne aucune ligne, on retourne une collection vide.
		final DtList<DtObject> dtc = new DtList<>(resultMetaData.getDtDefinition());
		while (resultSet.next()) {
			dto = resultMetaData.createDtObject();
			readDtObject(mapping, resultSet, dto, fields);
			dtc.add(dto);
		}
		return dtc;
	}

	private static DtObject doRetrieveDtObject(final SqlMapping mapping, final ResultSet resultset, final ResultMetaData resultMetaData) throws SQLException {
		final DtField[] fields = findFields(resultMetaData, resultset.getMetaData());

		if (resultset.next()) {
			//On est dans le cas de récupération d'un objet, un objet a été trouvé
			//On vérifie qu'il y en a au plus un.
			final DtObject dto = resultMetaData.createDtObject();
			readDtObject(mapping, resultset, dto, fields);
			if (resultset.next()) {
				throw createTooManyRowsException();
			}
			return dto;
		}
		return null;
		//On est dans le cas de récupération d'un objet, pas de résultat
		//throw new NoObjectFoundException();

	}

	private static void readDtObject(final SqlMapping mapping, final ResultSet resultSet, final DtObject dto, final DtField[] fields) throws SQLException {
		Object value;
		for (int i = 0; i < fields.length; i++) {
			value = mapping.getValueForResultSet(resultSet, i + 1, fields[i].getDomain().getDataType());
			fields[i].getDataAccessor().setValue(dto, value);
		}
	}

	private static DtField[] findFields(final ResultMetaData resultMetaData, final ResultSetMetaData resultSetMetaData) throws SQLException {
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
		//----------------------------------------------------------------------
		final String[] res = new String[resultSetMetaData.getColumnCount()];
		for (int i = 0; i < res.length; i++) {
			res[i] = resultSetMetaData.getColumnLabel(i + 1); //getColumnLabel permet de récupérer le nom adapté lors du select (avec un select truc as machin from xxx)
		}
		return res;
	}

	private static RuntimeException createTooManyRowsException() {
		return new RuntimeException("load TooManyRows");
	}
}
