package io.vertigo.dynamo.database.vendor;

import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.task.model.TaskEngine;

public interface SqlDialect {
	/**
	 * @return The operator for string concatenation.
	 */
	default String getConcatOperator() {
		return " || ";
	}

	/**
	 * Prépare la PK si il n'y a pas de système de sequence.
	 * @param entity Objet à sauvegarder (création ou modification)
	 */
	default Optional<String> preparePrimaryKey(final Entity entity, final String tableName, final String sequencePrefix) {
		return Optional.empty();
	}

	/**
	 * Creates the insert request.
	 *
	 * @param dtDefinition the dtDefinition
	 * @return the sql request
	 */
	String createInsertQuery(final DtDefinition dtDefinition, String sequencePrefix, String tableName);

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour limiter le resultat à {maxRows}.
	 * @param separator Séparateur de la close where à utiliser
	 * @param request Buffer de la requete
	 * @param maxRows Nombre de lignes max
	 */
	void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows);

	/**
	 * @param insert Si opération de type insert
	 * @return Classe du moteur de tache à utiliser
	 */
	Class<? extends TaskEngine> getTaskEngineClass(final boolean insert);

	/**
	 * Requête à exécuter pour faire un select for update. Doit pouvoir être surchargé pour tenir compte des
	 * spécificités de la base de données utilisée..
	 * @param tableName nom de la table
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

}
