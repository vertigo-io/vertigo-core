package io.vertigo.dynamox.task.sqlserver;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;

import java.sql.SQLException;

import javax.inject.Inject;

/**
 * Permet l'appel de requête insert en utilisant generatedKeys du PreparedStatement pour récupérer
 * la valeur de la clé primaire. Une tache utilisant cet engine ne traite pas les DtList.<br>
 * <br>
 * @author  jmainaud, evernat
 */
public class TaskEngineInsertWithGeneratedKeys extends AbstractTaskEngineSQL<KPreparedStatement> {

	/**
	 * Constructeur.
	 * @param scriptManager Manager de traitment de scripts
	 */
	@Inject
	public TaskEngineInsertWithGeneratedKeys(final ScriptManager scriptManager) {
		super(scriptManager);
	}

	/** {@inheritDoc} */
	@Override
	protected void checkSqlQuery(final String sql) {
		//Aucune vérification à priori.
	}

	/** {@inheritDoc} */
	@Override
	public int doExecute(final KConnection connection, final KPreparedStatement statement) throws SQLException {
		setParameters(statement);
		final int sqlRowcount = statement.executeUpdate();

		// gestion de generatedKey
		final DtObject dto = (DtObject) getValue("DTO");
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField pk = dtDefinition.getIdField().get();

		final Object key = statement.getGeneratedKey(pk.getName(), pk.getDomain());
		pk.getDataAccessor().setValue(dto, key);

		return sqlRowcount;
	}

	/** {@inheritDoc} */
	@Override
	protected final KPreparedStatement createStatement(final String sql, final KConnection connection) {
		final boolean generatedKeys = true;
		return getDataBaseManager().createPreparedStatement(connection, sql, generatedKeys);
	}
}
