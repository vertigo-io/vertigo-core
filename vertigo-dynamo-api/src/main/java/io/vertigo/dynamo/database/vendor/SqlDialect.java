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
package io.vertigo.dynamo.database.vendor;

import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * The database dialect.
 * Provides all the vendor's specific SQL
 * @author mlaroche
 *
 */
public interface SqlDialect {
	/**
	 * @return The operator for string concatenation.
	 */
	default String getConcatOperator() {
		return " || ";
	}

	/**
	 * Prépare la PK si il n'y a pas de système de sequence.
	 * @param tableName concerned table
	 * @param sequencePrefix the sequence prefix to use
	 * @return the query for selecting the pk
	 */
	default Optional<String> createPrimaryKeyQuery(final String tableName, final String sequencePrefix) {
		return Optional.empty();
	}

	/**
	 * Creates the insert request.
	 * @param dtDefinition the dtDefinition
	 * @param sequencePrefix the prefix to use for sequence
	 * @param tableName the name of the table in which we want to insert
	 * @return the sql request
	 */
	String createInsertQuery(final DtDefinition dtDefinition, String sequencePrefix, String tableName);

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour limiter le resultat à {maxRows}.
	 * @param query the sql query
	 * @param maxRows max rows
	 */
	void appendMaxRows(final StringBuilder query, final Integer maxRows);

	/**
	 * Requête à exécuter pour faire un select for update. Doit pouvoir être surchargé pour tenir compte des
	 * spécificités de la base de données utilisée..
	 * @param tableName nom de la table
	 * @param requestedFields the list of fields to retrieve (the select clause)
	 * @param idFieldName nom de la clé primaire
	 * @return select à exécuter.
	 */
	default String createSelectForUpdateQuery(final String tableName, final String requestedFields, final String idFieldName) {
		return new StringBuilder()
				.append(" select ").append(requestedFields)
				.append(" from ").append(tableName)
				.append(" where ").append(idFieldName).append(" = #").append(idFieldName).append('#')
				.append(" for update ")
				.toString();
	}

	/**
	 * @return if keys are generated
	 */
	boolean generatedKeys();
}
