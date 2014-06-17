/**
 * 
 */
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.database.connection.KConnection;
import io.vertigo.dynamo.database.statement.KCallableStatement;
import io.vertigo.dynamo.database.statement.KPreparedStatement;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.kernel.lang.Assertion;

import java.sql.SQLException;

import javax.inject.Inject;

/**
 * @author jmforhan
 * @version $Id: TaskEngineProcBatch.java,v 1.8 2014/01/24 17:59:38 pchretien Exp $
 */
public class TaskEngineProcBatch extends TaskEngineProc {

	/**
	 * Constructeur.
	 * @param scriptManager Manager de traitment de scripts
	 */
	@Inject
	public TaskEngineProcBatch(final ScriptManager scriptManager) {
		super(scriptManager);
	}

	/** {@inheritDoc} */
	@Override
	public int doExecute(final KConnection connection, final KCallableStatement statement) throws SQLException {
		// on alimente le batch.
		// La taille du batch est déduite de la taille de la collection en entrée.
		final int batchSize = getBatchSize();
		for (int i = 0; i < batchSize; i++) {
			setBatchParameters(statement, i);
			statement.addBatch();
		}

		return statement.executeBatch();
	}

	/**
	 * Modifie le statement en fonction des paramètres pour un statement qui sera exécuter en mode batch. Affecte les
	 * valeurs en entrée
	 * 
	 * @param statement de type KPreparedStatement, KCallableStatement...
	 * @param rowNumber ligne de DTC à prendre en compte
	 * @throws SQLException En cas d'erreur dans la configuration
	 */
	private void setBatchParameters(final KPreparedStatement statement, final int rowNumber) throws SQLException {
		Assertion.checkNotNull(statement);
		// ----------------------------------------------------------------------
		for (final TaskEngineSQLParam param : getParams()) {
			switch (param.getType()) {
				case IN:
				case INOUT:
					setParameter(statement, param, rowNumber);
					break;
				default:
					// On ne fait rien
					break;
			}
		}
	}

	private int getBatchSize() {
		Integer batchSize = null;
		for (final TaskAttribute attribute : getTaskDefinition().getAttributes()) {
			if (attribute.isIn() && attribute.getDomain().getDataType() == DataType.DtList) {
				Assertion.checkState(batchSize == null, "Pour un traitement Batch, il ne doit y avoir qu'une seule liste en entrée.");
				final DtList<?> dtc = getValue(attribute.getName());
				batchSize = dtc.size();
			}
		}
		if (batchSize == null) {
			throw new IllegalArgumentException("Pour un traitement Batch, il doit y avoir une (et une seule) liste en entrée.");
		}
		return batchSize;
	}
}
