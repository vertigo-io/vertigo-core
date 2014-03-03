package io.vertigo.dynamo.database.vendor;

import io.vertigo.dynamo.domain.metamodel.KDataType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface centralisant les mappings à la BDD.
 *
 * @author pchretien
 * @version $Id: SQLMapping.java,v 1.2 2014/01/20 17:45:43 pchretien Exp $
 */
public interface SQLMapping {
	/**
	 * Retourne le type correspondant à un type sql.
	 *
	 * @param typeSQL Type SQL
	 * @return Type Vertigo correspondant
	 */
	KDataType getDataType(int typeSQL);

	/**
	 * Retourne le type SQL correspondant à un type.
	 *
	 * @param dataType Type primitif
	 * @return Type SQL correspondant à un type
	 */
	int getTypeSQL(KDataType dataType);

	/**
	 * Affecte les valeurs sur un statement.
	 *
	 * @param statement Statement SQL à affecter
	 * @param index Index de la variable dans le statement
	 * @param dataType Type primitif
	 * @param value Valeur à affecter sur le statement à l'index indiqué
	 * @throws SQLException Exception sql
	 */
	void setValueOnStatement(PreparedStatement statement, int index, KDataType dataType, Object value) throws SQLException;

	/**
	 * Retourne la valeur typée vertigo d'un callablestatement.
	 *
	 * @param callableStatement CallableStatement SQL à affecter
	 * @param index Indexe de la variable dans le statement
	 * @param dataType Type primitif vertigo
	 * @return Valeur obtenue par le CallableStatement à l'indexe indiqué
	 * @throws SQLException Exception sql
	 */
	Object getValueForCallableStatement(CallableStatement callableStatement, int index, KDataType dataType) throws SQLException;

	/**
	 * Retourne la valeur typée vertigo d'un resultSet.
	 *
	 * @param rs ResultSet
	 * @param col Indexe de la colonne
	 * @param dataType Type primitif
	 * @return Valeur typée d'un resultSet
	 * @throws SQLException Exception sql
	 */
	Object getValueForResultSet(ResultSet rs, int col, KDataType dataType) throws SQLException;
}
