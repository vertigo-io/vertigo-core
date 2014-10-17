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

import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.statement.SqlStatementHandler;
import io.vertigo.lang.Assertion;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plugin intégrant la stratégie de création des objets issus d'un Select.
 * Ce plugin inclut deux stratégies
 * - Simple : la cible est connue on crée puis on peuple.
 * - Dynamic : la cible n'est pas connue, on crée dynamiquement un bean que l'on peuple.
 * @author  pchretien
 */
public final class SqlStatementHandlerImpl implements SqlStatementHandler {
	/** {@inheritDoc} */
	public SqlQueryResult retrieveData(final Domain domain, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		if (domain.getDataType().isPrimitive()) {
			return SqlRetrieveUtil.retrievePrimitive(domain.getDataType(), mapping, resultSet);
		}
		final SqlResultMetaData resultMetaData = createResultMetaData(domain, mapping, resultSet);
		return SqlRetrieveUtil.retrieveData(resultMetaData, mapping, resultSet);
	}

	/*
	 * Création du gestionnaire des types de sortie des preparedStatement.
	 */
	private static SqlResultMetaData createResultMetaData(final Domain domain, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		Assertion.checkArgument(!domain.getDataType().isPrimitive(), "le type de retour n''est ni un DTO ni une DTC");
		//---------------------------------------------------------------------
		//Il y a deux cas
		//Soit le DT est précisé alors le DTO ou DTC est typé de façon déclarative
		//Soit le DT n'est pas précisé alors le DTO ou DTC est typé de façon dynamique
		if (domain.hasDtDefinition()) {
			//Création des DTO, DTC typés de façon déclarative.
			return new SqlResultMetaDataStatic(domain.getDtDefinition(), DataType.DtObject.equals(domain.getDataType()));
		}
		//Création des DTO, DTC typés de façon dynamique.
		return new SqlResultMetaDataDynamic(DataType.DtObject.equals(domain.getDataType()), mapping, resultSet);
	}
}
