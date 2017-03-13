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
