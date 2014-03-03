package io.vertigo.dynamo.database.statement;

import java.sql.SQLException;

/**
 * Gestion des procédure stockées.
 *
 * @author pchretien
 * @version $Id: KCallableStatement.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface KCallableStatement extends KPreparedStatement {
	/**
	 * Getter générique.
	 * @param index Index du paramètre dans la requête SQL
	 * @throws SQLException Exception sql
	 * @return  Valeur du paramètre indexé
	 */
	Object getValue(final int index) throws SQLException;
}
