package io.vertigo.dynamo.database.vendor;

/**
 * Base de données.
 *
 * @author pchretien
 * @version $Id: DataBase.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
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
