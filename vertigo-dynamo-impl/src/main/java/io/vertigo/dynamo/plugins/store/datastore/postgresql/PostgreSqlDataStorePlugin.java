/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.store.datastore.postgresql;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.store.datastore.AbstractSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.sqlserver.TaskEngineInsertWithGeneratedKeys;
import io.vertigo.lang.Assertion;

/**
 * Implémentation d'un Store PostgreSQL.
 *
 * @author  pchretien
 */
public final class PostgreSqlDataStorePlugin extends AbstractSqlDataStorePlugin {
	private final String sequencePrefix;

	/**
	 * Constructor.
	 * @param nameOption the name of the dataSpace (optional)
	 * @param connectionName the name of the connection
	 * @param taskManager the taskManager
	 */
	@Inject
	public PostgreSqlDataStorePlugin(@Named("name") final Optional<String> nameOption, @Named("connectionName") final Optional<String> connectionName, @Named("sequencePrefix") final String sequencePrefix, final TaskManager taskManager) {
		super(nameOption, connectionName, taskManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//-----
		this.sequencePrefix = sequencePrefix;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return insert ? TaskEngineInsertWithGeneratedKeys.class : TaskEngineProc.class;
	}

	private String getSequenceName(final DtDefinition dtDefinition) {
		return sequencePrefix + getTableName(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {

		final String tableName = getTableName(dtDefinition);
		final StringBuilder request = new StringBuilder()
				.append("insert into ").append(tableName).append(" (");

		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator)
						.append(dtField.getName());
				separator = ", ";
			}
		}
		request.append(") values (");
		separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				if (dtField.getType() != DtField.FieldType.ID) {
					request.append(" #DTO.").append(dtField.getName()).append('#');
				} else {
					request.append("nextval('").append(getSequenceName(dtDefinition)).append("')");
				}
				separator = ", ";
			}
		}
		request.append(");");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(" limit ").append(maxRows);
	}
}
