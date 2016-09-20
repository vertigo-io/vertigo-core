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
package io.vertigo.dynamo.plugins.store.datastore.sqlserver;

import java.util.Optional;
import java.util.stream.Collectors;

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
 * Implémentation d'un Store MS Sql Server.
 * Dans le cas de SQL Server, la gestion des clés n'est pas assurée par des séquences.
 *
 * @author  jmainaud, evernat
 */
public final class SqlServerDataStorePlugin extends AbstractSqlDataStorePlugin {
	/**
	 * Constructor.
	 * @param nameOption the name of the dataSpace (optional)
	 * @param connectionName the name of the connection
	 * @param taskManager the taskManager
	 */
	@Inject
	public SqlServerDataStorePlugin(@Named("name") final Optional<String> nameOption, @Named("connectionName") final Optional<String> connectionName, final TaskManager taskManager) {
		super(nameOption, connectionName, taskManager);
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		Assertion.checkArgument(request.indexOf("select ") == 0, "request doit commencer par select");
		//-----
		request.insert("select ".length(), " top " + maxRows + ' ');
	}

	@Override
	protected String getConcatOperator() {
		return " + ";
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return insert ? TaskEngineInsertWithGeneratedKeys.class : TaskEngineProc.class;
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {

		final String tableName = getTableName(dtDefinition);
		return new StringBuilder()
				.append("insert into ").append(tableName).append(" ( ")
				.append(dtDefinition.getFields()
						.stream()
						.filter(dtField -> dtField.isPersistent() && dtField.getType() != DtField.FieldType.ID)
						.map(dtField -> dtField.getName())
						.collect(Collectors.joining(", ")))
				.append(") values (")
				.append(") values ( ")
				.append(dtDefinition.getFields()
						.stream()
						.filter(dtField -> dtField.isPersistent() && dtField.getType() != DtField.FieldType.ID)
						.map(dtField -> " #DTO." + dtField.getName() + '#')
						.collect(Collectors.joining(", ")))
				.append(") ")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected String getSelectForUpdate(final String tableName, final String requestedFields, final String idFieldName) {
		return new StringBuilder()
				.append(" select ").append(requestedFields).append(" from ")
				.append(tableName)
				.append(" WITH (UPDLOCK, INDEX(PK_").append(tableName).append(")) ")
				.append(" where ").append(idFieldName).append(" = #").append(idFieldName).append('#')
				.toString();
	}
}
