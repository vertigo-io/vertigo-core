/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamox.task.sqlserver;

import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

import javax.inject.Inject;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.parser.SqlNamedParam;
import io.vertigo.database.sql.statement.SqlPreparedStatement;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;

/**
 * Permet l'appel de requête insert en utilisant generatedKeys du PreparedStatement pour récupérer
 * la valeur de la clé primaire. Une tache utilisant cet engine ne traite pas les DtList.<br>
 * <br>
 * @author  jmainaud, evernat
 */
public class TaskEngineInsertWithGeneratedKeys extends AbstractTaskEngineSQL {

	/**
	 * Constructor.
	 * @param scriptManager scriptManager
	 * @param transactionManager transactionManager
	 * @param storeManager storeManager
	 * @param sqlDataBaseManager sqlDataBaseManager
	 */
	@Inject
	public TaskEngineInsertWithGeneratedKeys(
			final ScriptManager scriptManager,
			final VTransactionManager transactionManager,
			final StoreManager storeManager,
			final SqlDataBaseManager sqlDataBaseManager) {
		super(scriptManager, transactionManager, storeManager, sqlDataBaseManager);
	}

	/** {@inheritDoc} */
	@Override
	public OptionalInt doExecute(
			final String sql,
			final SqlConnection connection,
			final SqlPreparedStatement statement,
			final List<SqlNamedParam> params) throws SQLException {
		Assertion.checkArgNotEmpty(sql);
		Assertion.checkNotNull(connection);
		Assertion.checkNotNull(statement);
		Assertion.checkNotNull(params);
		//--
		final GenerationMode generationMode = connection.getDataBase().getSqlDialect().getGenerationMode();

		// gestion de generatedKey
		final Entity entity = getValue("DTO");

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final DtField idField = dtDefinition.getIdField().get();

		final Tuples.Tuple2<Integer, ?> result = statement
				.executeUpdateWithGeneratedKey(sql, buildParameters(params), generationMode, idField.getName(), idField.getDomain().getDataType().getJavaClass());

		final Object id = result.getVal2();
		idField.getDataAccessor().setValue(entity, id);
		//---
		return /*sqlRowcount*/ OptionalInt.of(result.getVal1());
	}
}
