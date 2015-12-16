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
package io.vertigo.dynamo.plugins.store.datastore.oracle;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.store.datastore.AbstractSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation d'un Store Oracle.
 * Dans le cas de Oracle, la gestion des clés est assurée par des séquences.
 *
 * @author pchretien
 */
public final class OracleDataStorePlugin extends AbstractSqlDataStorePlugin {
	private final String sequencePrefix;

	/**
	 * Constructor.
	 * @param nameOption the name of the dataSpace (optional)
	 * @param connectionName the name of the connection
	 * @param taskManager the taskManager
	 */
	@Inject
	public OracleDataStorePlugin(@Named("name") final Option<String> nameOption, @Named("connectionName") final Option<String> connectionName, @Named("sequencePrefix") final String sequencePrefix, final TaskManager taskManager) {
		super(nameOption, connectionName, taskManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//-----
		this.sequencePrefix = sequencePrefix;
	}

	/**
	 * Nom de la séquence utilisée lors des inserts
	 * @param dtDefinition Définition du DT mappé
	 * @return String Nom de la sequence
	 */
	private String getSequenceName(final DtDefinition dtDefinition) {
		//oracle n'autorise pas de sequence de plus de 30 char.
		String seqName = sequencePrefix + getTableName(dtDefinition);
		if (seqName.length() > 30) {
			seqName = seqName.substring(0, 30);
		}
		return seqName;
	}

	/** {@inheritDoc} */
	@Override
	protected Class<? extends TaskEngine> getTaskEngineClass(final boolean insert) {
		return TaskEngineProc.class;
	}

	/** {@inheritDoc} */
	@Override
	protected String createInsertQuery(final DtDefinition dtDefinition) {
		final DtField pk = dtDefinition.getIdField().get();

		final String tableName = getTableName(dtDefinition);
		final StringBuilder request = new StringBuilder("begin insert into ").append(tableName).append(" ( ");

		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator)
						.append(dtField.getName());
				separator = ", ";
			}
		}
		request.append(") values ( ");
		separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			if (dtField.isPersistent()) {
				request.append(separator);
				if (dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
					request.append(" #DTO.").append(dtField.getName()).append('#');
				} else {
					request.append(getSequenceName(dtDefinition)).append(".nextval ");
				}
				separator = ", ";
			}
		}
		request.append(") returning ").append(pk.getName()).append(" into %DTO.").append(pk.getName()).append("%;").append("end;");
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum <= ").append(maxRows.toString());
	}

}
