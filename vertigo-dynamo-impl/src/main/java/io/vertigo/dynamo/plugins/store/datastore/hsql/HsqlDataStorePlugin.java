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
package io.vertigo.dynamo.plugins.store.datastore.hsql;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.plugins.store.datastore.AbstractSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un Store HSQLDB.
 * Dans le cas de HSQLDB, la gestion des clés est assurée par des séquences.
 *
 * @author  pchretien
 */
public final class HsqlDataStorePlugin extends AbstractSqlDataStorePlugin {
	private static final String DTO_SEQUENCE = "DTO_SEQUENCE";
	private static final String SEQUENCE_FIELD = "SEQUENCE";
	/**
	 * Prefix de la tache : SELECT
	 */
	private static final String TK_SELECT = "TK_SELECT";

	/**
	 * Domaine à usage interne.
	 * Ce domaine n'est pas enregistré.
	 */
	private final Domain resultDomain = new Domain("DO_HSQL", DataType.DtObject);
	private final String sequencePrefix;

	/**
	 * Constructor.
	 * @param dataSpaceOption the dataSpace (optional)
	 * @param connectionName the name of the connection
	 * @param taskManager the taskManager
	 * @param sequencePrefix the prefix used by the sequence
	 */
	@Inject
	public HsqlDataStorePlugin(@Named("dataSpace") final Option<String> dataSpaceOption, @Named("connectionName") final Option<String> connectionName, @Named("sequencePrefix") final String sequencePrefix, final TaskManager taskManager) {
		super(dataSpaceOption, connectionName, taskManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//-----
		this.sequencePrefix = sequencePrefix;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return TaskEngineProc.class;
	}

	private Long getSequenceNextval(final String sequenceName) {
		final String taskName = TK_SELECT + '_' + sequenceName;

		final StringBuilder request = chooseDataBaseStyle(sequenceName);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withDataSpace(getDataSpace())
				.withRequest(request.toString())
				.withOutAttribute(DTO_SEQUENCE, resultDomain, true)// OUT, obligatoire
				.build();

		final Task task = new TaskBuilder(taskDefinition).build();

		final DtObject dto = getTaskManager()
				.execute(task)
				.getResult();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField dtField = dtDefinition.getField(SEQUENCE_FIELD);

		return Long.valueOf((Integer) dtField.getDataAccessor().getValue(dto));
	}

	private static StringBuilder chooseDataBaseStyle(final String sequenceName) {
		return new StringBuilder("select next value for " + sequenceName + "  as " + SEQUENCE_FIELD)
				.append(" from information_schema.system_sequences where sequence_name = upper('" + sequenceName + "')");
	}

	/** {@inheritDoc} */
	@Override
	protected void preparePrimaryKey(final DtObject dto) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		final DtField pk = dtDefinition.getIdField().get();
		pk.getDataAccessor().setValue(dto, getSequenceNextval(sequencePrefix + getTableName(dtDefinition)));
		//executeInsert(transaction, dto);
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
				request.append(separator);
				request.append(dtField.getName());
				separator = ", ";
			}
		}
		request.append(") values (");
		separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				request.append(" #DTO.").append(dtField.getName()).append('#');
				separator = ", ";
			}
		}
		request.append(");");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum <= ").append(maxRows.toString());
	}
}
