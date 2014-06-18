package io.vertigo.dynamo.database.vendor;

/**
 * Base de données.
 *
 * @author pchretien
 */
public interface DataBase {
	/**
	 * @return Handler d'exception associé à la base de données.
	 */
	SQLExceptionHandler getSqlExceptionHandler();

	/**
	 * @return Mapping sql associé à la base de données
	 */
	SQLMapping getSqlMapping();
}
