package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.database.statement.QueryResult;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.Domain;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plugin permettant de créer les résultats issus d'une requête SQL de type select.
 * 
 * @author  pchretien
 * @version $Id: StatementHandler.java,v 1.2 2014/01/20 17:46:01 pchretien Exp $
 */
public interface StatementHandler {
	/**
	 * Création du résultat issu d'un resultSet.
	 * @param domain Domain résultat
	 * @param mapping Mapping SQL
	 * @param resultSet ResultSet comprenant résultat et Metadonnées permettant le cas échéant de créer dynamiquement un type dynamiquement.
	 * @return Résultat de la requête.
	 * @throws SQLException Exception SQL
	 */
	QueryResult retrieveData(final Domain domain, final SQLMapping mapping, final ResultSet resultSet) throws SQLException;
}
