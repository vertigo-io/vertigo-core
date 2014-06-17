package io.vertigo.dynamo.impl.database.statementhandler;

import io.vertigo.dynamo.database.statement.QueryResult;
import io.vertigo.dynamo.database.vendor.SQLMapping;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.impl.database.statement.StatementHandler;
import io.vertigo.kernel.lang.Assertion;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Plugin intégrant la stratégie de création des objets issus d'un Select.
 * Ce plugin inclut deux stratégies
 * - Simple : la cible est connue on crée puis on peuple.
 * - Dynamic : la cible n'est pas connue, on crée dynamiquement un bean que l'on peuple.
 * @author  pchretien
 */
public final class StatementHandlerImpl implements StatementHandler {
	/** {@inheritDoc} */
	public QueryResult retrieveData(final Domain domain, final SQLMapping mapping, final ResultSet resultSet) throws SQLException {
		if (domain.getDataType().isPrimitive()) {
			return RetrieveUtil.retrievePrimitive(domain.getDataType(), mapping, resultSet);
		}
		final ResultMetaData resultMetaData = createResultMetaData(domain, mapping, resultSet);
		return RetrieveUtil.retrieveData(resultMetaData, mapping, resultSet);
	}

	/*
	 * Création du gestionnaire des types de sortie des preparedStatement.
	 */
	private static ResultMetaData createResultMetaData(final Domain domain, final SQLMapping mapping, final ResultSet resultSet) throws SQLException {
		Assertion.checkArgument(!domain.getDataType().isPrimitive(), "le type de retour n''est ni un DTO ni une DTC");
		//---------------------------------------------------------------------
		//Il y a deux cas
		//Soit le DT est précisé alors le DTO ou DTC est typé de façon déclarative
		//Soit le DT n'est pas précisé alors le DTO ou DTC est typé de façon dynamique
		if (domain.hasDtDefinition()) {
			//Création des DTO, DTC typés de façon déclarative.
			return new SimpleResultMetaData(domain.getDtDefinition(), DataType.DtObject.equals(domain.getDataType()));
		}
		//Création des DTO, DTC typés de façon dynamique.
		return new DynamicResultMetaData(DataType.DtObject.equals(domain.getDataType()), mapping, resultSet);
	}
}
