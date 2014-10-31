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
package io.vertigo.dynamo.plugins.persistence.oracle;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.persistence.AbstractSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.lang.Assertion;

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
	 * Constructeur.
	 * @param sequencePrefix Configuration du préfixe de la séquence
	 */
	@Inject
	public OracleDataStorePlugin(@Named("sequencePrefix") final String sequencePrefix, final TaskManager taskManager) {
		super(taskManager);
		Assertion.checkArgNotEmpty(sequencePrefix);
		//---------------------------------------------------------------------
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

	@Override
	protected void beforeInsert(final StringBuilder request) {
		request.append("begin ");
	}

	@Override
	protected void afterInsert(final StringBuilder request, final DtDefinition dtDefinition) {
		final DtField pk = dtDefinition.getIdField().get();
		request.append(" returning ").append(pk.getName()).append(" into %DTO.").append(pk.getName()).append("%;").append("end;");
	}

	@Override
	protected boolean acceptOnInsert(final DtField dtField) {
		return dtField.isPersistent();
	}

	@Override
	protected void onPrimaryKey(final StringBuilder request, final DtDefinition dtDefinition, final DtField dtField) {
		request.append(getSequenceName(dtDefinition)).append(".nextval ");
	}

	/** {@inheritDoc} */
	@Override
	protected void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows) {
		request.append(separator).append(" rownum <= ").append(maxRows.toString());
	}
}
