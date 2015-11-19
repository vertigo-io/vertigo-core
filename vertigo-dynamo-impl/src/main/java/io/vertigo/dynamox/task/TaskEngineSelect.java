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
package io.vertigo.dynamox.task;

import io.vertigo.commons.script.ScriptManager;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlPreparedStatement;
import io.vertigo.dynamo.database.statement.SqlQueryResult;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.VSystemException;

import java.sql.SQLException;

import javax.inject.Inject;

/**
 * Permet de réaliser des requêtes sur un base de données.<br>
 * <br>
 * Paramètres d'entrée : n String, Date, Boolean, Double, Integer ou DTO, DTC<br>
 * Paramètres de sorties : 1 DTO <u>ou</u> DTC <u>
 * <br>
 * Dans le cas d'un DtObject en sortie, la requête SQL doit ramener un et un seul
 * enregistrement. Dans le cas contraire, la méthode execute() de la classe service
 * remontera un SQLException().<br>
 * <br>
 * Chaine de configuration :<br>
 * La chaine de configuration utilise les délimiteurs #NOM# pour les paramètres.
 * L'utilisation d'une valeur d'un DtObject est déclarée par #DTOBJECT.FIELD#.
 * Le paramètre de sortie n'apparaît pas dans la chaine de configuration.<br>
 * <br>
 * Un DtObject d'entrée peut être utilisé pour la sortie et est alors déclaré en
 * entrée/sortie.
 *
 * @author  FCONSTANTIN
 */
public class TaskEngineSelect extends AbstractTaskEngineSQL<SqlPreparedStatement> {

	/**
	 * Constructor.
	 */
	@Inject
	public TaskEngineSelect(final ScriptManager scriptManager, final VTransactionManager transactionManager, final StoreManager storeManager, final SqlDataBaseManager sqlDataBaseManager) {
		super(scriptManager, transactionManager, storeManager, sqlDataBaseManager);
	}

	/*
	 * Récupération de l'attribut OUT. Il doit être unique.
	 */
	private TaskAttribute getOutTaskAttribute() {
		if (getTaskDefinition().getOutAttributeOption().isEmpty()) {
			throw new VSystemException("TaskEngineSelect must have at least on DtObject or one DtList!");
		}
		return getTaskDefinition().getOutAttributeOption().get();

	}

	/** {@inheritDoc} */
	@Override
	protected int doExecute(final SqlConnection connection, final SqlPreparedStatement statement) throws SQLException {
		setInParameters(statement);
		final TaskAttribute outAttribute = getOutTaskAttribute();

		final SqlQueryResult result = statement.executeQuery(outAttribute.getDomain());
		setResult(result.getValue());
		return result.getSQLRowCount();
	}

	/** {@inheritDoc} */
	@Override
	protected final SqlPreparedStatement createStatement(final String sql, final SqlConnection connection) {
		return getDataBaseManager().createPreparedStatement(connection, sql, false);
	}
}
