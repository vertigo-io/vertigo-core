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

import java.sql.ResultSet;
import java.sql.SQLException;

import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.lang.Assertion;

/**
 * Plugin intégrant la stratégie de création des objets issus d'un Select.
 * Ce plugin inclut deux stratégies
 * - Simple : la cible est connue on crée puis on peuple.
 * @author  pchretien
 */
final class SqlStatementHandler {
	/**
	 * Création du résultat issu d'un resultSet.
	 * @param domain Domain résultat
	 * @param mapping Mapping SQL
	 * @param resultSet ResultSet comprenant résultat et Metadonnées permettant le cas échéant de créer dynamiquement un type dynamiquement.
	 * @return Résultat de la requête.
	 * @throws SQLException Exception SQL
	 */
	static SqlQueryResult retrieveData(final Domain domain, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (domain.getDataType().isPrimitive()) {
			return SqlRetrieveUtil.retrievePrimitive(domain.getDataType(), mapping, resultSet);
		}
		final SqlResultMetaData resultMetaData = createResultMetaData(domain, mapping, resultSet);
		return SqlRetrieveUtil.retrieveData(resultMetaData, mapping, resultSet);
	}

	/*
	 * Création du gestionnaire des types de sortie des preparedStatement.
	 */
	private static SqlResultMetaData createResultMetaData(final Domain domain, final SqlMapping mapping, final ResultSet resultSet) {
		Assertion.checkArgument(!domain.getDataType().isPrimitive(), "le type de retour n''est ni un DTO ni une DTC");
		//-----
		final boolean isDtObject = DataType.DtObject.equals(domain.getDataType());
		//Création des DTO, DTC typés de façon déclarative.
		return new SqlResultMetaData(domain.getDtDefinition(), isDtObject);
	}
}
