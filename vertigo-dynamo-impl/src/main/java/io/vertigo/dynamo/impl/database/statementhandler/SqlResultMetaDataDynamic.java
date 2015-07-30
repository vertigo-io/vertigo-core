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

import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
* Gestion dynamiques des DTO et DTC en sortie.
* Dans le cas des selects avec un type de sortie générique (DTO ou DTC) ;
* il convient de fabriquer dynamiquement, à la volée les DT et DTO, DTC en sortie.
*
* @author  pchretien
*/
final class SqlResultMetaDataDynamic implements SqlResultMetaData {
	private final boolean isDtObject;
	private final SerializableDtDefinition serializableDefinition;

	/**
	 * Constructeur.
	 */
	SqlResultMetaDataDynamic(final boolean isDtObject, final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		Assertion.checkNotNull(mapping);
		Assertion.checkNotNull(resultSet);
		//-----
		this.isDtObject = isDtObject;
		serializableDefinition = createSerializableDtDefinition(mapping, resultSet);
	}

	/** {@inheritDoc} */
	@Override
	public DtObject createDtObject() {
		return new SqlDynamicDtObject(serializableDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDtObject() {
		return isDtObject;
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition getDtDefinition() {
		return serializableDefinition.getDtDefinition();
	}

	//=========================================================================
	//==============Construction de SerializableDtDefinition===================
	//=========================================================================

	private static SerializableDtDefinition createSerializableDtDefinition(final SqlMapping mapping, final ResultSet resultSet) throws SQLException {
		final ResultSetMetaData metaData = resultSet.getMetaData();
		String fieldName;
		String fieldLabel;
		DataType localDataType;
		final SerializableDtField[] fields = new SerializableDtField[metaData.getColumnCount()];
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			//On passe les champs en maj pour postgreSQL et SQLServer.
			fieldName = metaData.getColumnName(i).toUpperCase();
			//On vérifie que la colonne possède un nom signifiant
			Assertion.checkArgNotEmpty(fieldName, "Une des colonnes de la requête ne possède ni nom ni alias.");
			//-----
			fieldLabel = metaData.getColumnLabel(i);
			localDataType = mapping.getDataType(metaData.getColumnType(i));
			fields[i - 1] = new SerializableDtField(fieldName, fieldLabel, localDataType);
		}
		return new SerializableDtDefinition(fields);
	}
}
