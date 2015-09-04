/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.lang.Assertion;

import java.sql.SQLException;

import javax.inject.Inject;

/**
 * @author jmforhan
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
	public int doExecute(final SqlConnection connection, final SqlCallableStatement statement) throws SQLException {
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
	private void setBatchParameters(final SqlPreparedStatement statement, final int rowNumber) throws SQLException {
		Assertion.checkNotNull(statement);
		//-----
		for (final TaskEngineSQLParam param : getParams()) {
			switch (param.getType()) {
				case IN:
				case INOUT:
					setParameter(statement, param, rowNumber);
					break;
				case OUT:
				default:
					// On ne fait rien
					break;
			}
		}
	}

	private int getBatchSize() {
		Integer batchSize = null;
		for (final TaskAttribute attribute : getTaskDefinition().getInAttributes()) {
			if (attribute.getDomain().getDataType() == DataType.DtList) {
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
