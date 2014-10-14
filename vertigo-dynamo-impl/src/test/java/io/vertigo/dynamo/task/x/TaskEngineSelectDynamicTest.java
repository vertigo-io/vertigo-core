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
package io.vertigo.dynamo.task.x;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamox.task.TaskEngineSelect;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author npiedeloup
 */
public final class TaskEngineSelectDynamicTest extends AbstractTestCaseJU4 {
	@Inject
	private TaskManager taskManager;
	@Inject
	private PersistenceManager persistenceManager;
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	private KTransactionManager transactionManager;

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table famille
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "create table famille(fam_id BIGINT , LIBELLE varchar(255));");
		execCallableStatement(connection, "create sequence SEQ_FAMILLE start with 10001 increment by 1");

		addNFamille(10);
	}

	private void addNFamille(final int nbFamille) {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//----------------------
			for (int i = 0; i < nbFamille; i++) {
				final Famille famille = new Famille();
				famille.setLibelle("encore un (" + i + ")");
				persistenceManager.getBroker().save(famille);
			}
			transaction.commit();
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		//A chaque fin de test on arrète la base.
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "shutdown;");
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql);
		callableStatement.init();
		callableStatement.executeUpdate();
	}

	/**
	 * Test de double exécution d'une tache.
	 * @throws Exception erreur
	 */
	@Test
	public void testScript() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_SCRIPT_TEST = "TK_SCRIPT_TEST";
			registerTaskObject(TK_SCRIPT_TEST, "select * from FAMILLE fam <%if(false) {%>where fam.FAM_ID = #DTO_FAMILLE.FAM_ID#<%}%>");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_SCRIPT_TEST, TaskDefinition.class);

			final Famille famille = new Famille();
			famille.setFamId(10001L + 1);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTO_FAMILLE", famille)//
					.build();
			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test des scripts.
	 * @throws Exception erreur
	 */
	@Test
	public void testScriptVar() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_SCRIPT_TEST = "TK_SCRIPT_TEST";
			registerTaskObject(TK_SCRIPT_TEST, "select * from FAMILLE fam <%if(dtoFamille.getFamId() == 10002L) {%>where fam.FAM_ID = #DTO_FAMILLE.FAM_ID#<%}%>");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_SCRIPT_TEST, TaskDefinition.class);

			final Famille famille = new Famille();
			famille.setFamId(10001L + 1);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTO_FAMILLE", famille)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(1, resultList.size());
			Assert.assertEquals(10001L + 1, resultList.get(0).getFamId().longValue());
		}
	}

	/**
	 * Test des nullable.
	 * @throws Exception erreur
	 */
	@Test
	public void testNullable() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_NULLABLE_TEST = "TK_NULLABLE_TEST";
			registerTaskWithNullableIn(TK_NULLABLE_TEST, "select * from FAMILLE fam where fam.FAM_ID = #PARAM_1#<%if(param2!=null) {%> OR fam.FAM_ID = #PARAM_2#+2 <%}%><%if(param3!=null) {%> OR fam.FAM_ID = #PARAM_3#+3<%}%>");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_NULLABLE_TEST, TaskDefinition.class);

			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("PARAM_1", 10002)//
					.withValue("PARAM_2", null)//
					.withValue("PARAM_3", 10002)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(2, resultList.size());
			Assert.assertEquals(10002L, resultList.get(0).getFamId().longValue());
			Assert.assertEquals(10002L + 3, resultList.get(1).getFamId().longValue());
		}
	}

	/**
	 * Test des scripts.
	 * @throws Exception erreur
	 */
	@Test
	public void testScriptVarList() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_SCRIPT_TEST = "TK_SCRIPT_TEST";
			registerTaskList(TK_SCRIPT_TEST, "select * from FAMILLE fam <%if(!dtcFamilleIn.isEmpty()) {%>where fam.FAM_ID in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)<%}%>");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_SCRIPT_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();
			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test du preprocessor trim.
	 * Note: nous n'avons pas accès à la chaine trimée, on check juste que la requete est valide.
	 * @throws Exception erreur
	 */
	@Test
	public void testTrim() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_SCRIPT_TEST = "TK_SCRIPT_TEST";
			registerTaskObject(TK_SCRIPT_TEST, "select * from FAMILLE fam \n<%if(false) {%>\nwhere fam.FAM_ID = #DTO_FAMILLE.FAM_ID#\n<%}%>\n");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_SCRIPT_TEST, TaskDefinition.class);

			final Famille famille = new Famille();
			famille.setFamId(10001L + 1);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTO_FAMILLE", famille)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereIn() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			familleIds.add(createFamId(10001L + 1));
			familleIds.add(createFamId(10001L + 3));
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(2, resultList.size());
			Assert.assertEquals(10001L + 1, resultList.get(0).getFamId().longValue());
			Assert.assertEquals(10001L + 3, resultList.get(1).getFamId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereInEmpty() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();
			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(0, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereNotIn() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID not in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds).build();

			familleIds.add(createFamId(10001L + 1));
			familleIds.add(createFamId(10001L + 3));
			familleIds.add(createFamId(10001L + 5));
			familleIds.add(createFamId(10001L + 6));
			familleIds.add(createFamId(10001L + 7));
			familleIds.add(createFamId(10001L + 8));
			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(4, resultList.size());
			Assert.assertEquals(10001L + 0, resultList.get(0).getFamId().longValue());
			Assert.assertEquals(10001L + 2, resultList.get(1).getFamId().longValue());
			Assert.assertEquals(10001L + 4, resultList.get(2).getFamId().longValue());
			Assert.assertEquals(10001L + 9, resultList.get(3).getFamId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereNotInEmpty() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID not in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a inclure.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereIn2200() {
		addNFamille(4500);

		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			for (int i = 0; i < 2200; i++) {
				familleIds.add(createFamId(10001L + 2 * i));
			}
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(2200, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a exclure.
	 * @throws Exception erreur
	 */
	@Test
	public void testWhereNotIn2200() {
		addNFamille(4500);

		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final String TK_WHERE_ID_TEST = "TK_WHERE_ID_TEST";
			registerTaskList(TK_WHERE_ID_TEST, "select * from FAMILLE fam where fam.FAM_ID not in (#DTC_FAMILLE_IN.ROWNUM.FAM_ID#)");
			final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_WHERE_ID_TEST, TaskDefinition.class);

			final DtList<Famille> familleIds = new DtList<>(Famille.class);
			for (int i = 0; i < 2200; i++) {
				familleIds.add(createFamId(10001L + 2 * i));
			}
			final Task task = new TaskBuilder(taskDefinition)//
					.withValue("DTC_FAMILLE_IN", familleIds)//
					.build();

			// on suppose un appel synchrone : getResult immédiat.
			final TaskResult result = taskManager.execute(task);

			final DtList<Famille> resultList = result.<DtList<Famille>> getValue("DTC_FAMILLE_OUT");
			Assert.assertEquals(10 + 4500 - 2200, resultList.size());
		}
	}

	private static Famille createFamId(final long id) {
		final Famille famille = new Famille();
		famille.setFamId(id);
		return famille;
	}

	private static TaskDefinition registerTaskWithNullableIn(final String taskDefinitionName, final String params) {
		final Domain doInteger = Home.getDefinitionSpace().resolve("DO_INTEGER", Domain.class);
		final Domain doFamilleList = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTC", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(TaskEngineSelect.class)//
				.withRequest(params)//
				.withPackageName(TaskEngineSelect.class.getPackage().getName())//
				.withAttribute("PARAM_1", doInteger, true, true)//
				.withAttribute("PARAM_2", doInteger, false, true)//
				.withAttribute("PARAM_3", doInteger, false, true)//
				.withAttribute("DTC_FAMILLE_OUT", doFamilleList, true, false)//
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	private static TaskDefinition registerTaskObject(final String taskDefinitionName, final String params) {
		final Domain doFamilleList = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTC", Domain.class);
		final Domain doFamille = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTO", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(TaskEngineSelect.class)//
				.withRequest(params)//
				.withPackageName(TaskEngineSelect.class.getPackage().getName())//
				.withAttribute("DTO_FAMILLE", doFamille, true, true)//
				.withAttribute("DTC_FAMILLE_OUT", doFamilleList, true, false)//
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	private static TaskDefinition registerTaskList(final String taskDefinitionName, final String params) {
		final Domain doFamilleList = Home.getDefinitionSpace().resolve("DO_DT_FAMILLE_DTC", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(TaskEngineSelect.class)//
				.withRequest(params)//
				.withPackageName(TaskEngineSelect.class.getPackage().getName())//
				.withAttribute("DTC_FAMILLE_IN", doFamilleList, true, true)//
				.withAttribute("DTC_FAMILLE_OUT", doFamilleList, true, false)//
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

}
