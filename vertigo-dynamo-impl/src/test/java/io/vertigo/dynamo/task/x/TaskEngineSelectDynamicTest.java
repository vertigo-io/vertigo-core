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
import io.vertigo.dynamo.task.data.SuperHero;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
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
	private static final String DTC_SUPER_HERO_IN = "DTC_SUPER_HERO_IN";
	private static final String DO_INTEGER = "DO_INTEGER";
	private static final String DO_DT_SUPER_HERO_DTO = "DO_DT_SUPER_HERO_DTO";
	private static final String DO_DT_SUPER_HERO_DTC = "DO_DT_SUPER_HERO_DTC";
	private static final String DTO_SUPER_HERO = "DTO_SUPER_HERO";
	private static final String DTC_SUPER_HERO_OUT = "DTC_SUPER_HERO_OUT";
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
		//A chaque test on recrée la table SUPER_HERO
		final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
		execCallableStatement(connection, "create table SUPER_HERO(id BIGINT , name varchar(255));");
		execCallableStatement(connection, "create sequence SEQ_SUPER_HERO start with 10001 increment by 1");

		addNSuperHero(10);
	}

	private void addNSuperHero(final int size) {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//-----
			for (int i = 0; i < size; i++) {
				final SuperHero superHero = new SuperHero();
				superHero.setName("SuperHero ( " + i + ")");
				persistenceManager.getBroker().create(superHero);
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
		try (final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql)) {
			callableStatement.init();
			callableStatement.executeUpdate();
		}
	}

	/**
	 * Test de double exécution d'une tache.
	 */
	@Test
	public void testScript() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(false) {%>where ID = #DTO_SUPER_HERO.ID#<%}%>");

			final SuperHero superHero = createSuperHero(10001L + 1);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTO_SUPER_HERO, superHero)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVar() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(dtoSuperHero.getId() == 10002L) {%>where ID = #DTO_SUPER_HERO.ID#<%}%>");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTO_SUPER_HERO, superHero)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(1, resultList.size());
			Assert.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
		}
	}

	/**
	 * Test des nullable.
	 */
	@Test
	public void testNullable() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskWithNullableIn("TK_NULLABLE_TEST",
					"select * from SUPER_HERO where ID = #PARAM_1#<%if(param2!=null) {%> OR ID = #PARAM_2#+2 <%}%><%if(param3!=null) {%> OR ID = #PARAM_3#+3<%}%>");

			final Task task = new TaskBuilder(taskDefinition)
					.withValue("PARAM_1", 10002)
					.withValue("PARAM_2", null)
					.withValue("PARAM_3", 10002)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(2, resultList.size());
			Assert.assertEquals(10002L, resultList.get(0).getId().longValue());
			Assert.assertEquals(10002L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVarList() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(!dtcSuperHeroIn.isEmpty()) {%>where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)<%}%>");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test du preprocessor trim.
	 * Note: nous n'avons pas accès à la chaine trimée, on check juste que la requete est valide.
	 */
	@Test
	public void testTrim() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO  \n<%if(false) {%>\nwhere ID = #DTO_SUPER_HERO.ID#\n<%}%>\n");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTO_SUPER_HERO, superHero)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereIn() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			ids.add(createSuperHero(10001L + 1));
			ids.add(createSuperHero(10001L + 3));

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(2, resultList.size());
			Assert.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assert.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInTab() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where\tID in\t(#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			ids.add(createSuperHero(10001L + 1));
			ids.add(createSuperHero(10001L + 3));

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(2, resultList.size());
			Assert.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assert.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInEmpty() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(0, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotIn() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids).build();

			ids.add(createSuperHero(10001L + 1));
			ids.add(createSuperHero(10001L + 3));
			ids.add(createSuperHero(10001L + 5));
			ids.add(createSuperHero(10001L + 6));
			ids.add(createSuperHero(10001L + 7));
			ids.add(createSuperHero(10001L + 8));

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(4, resultList.size());
			Assert.assertEquals(10001L + 0, resultList.get(0).getId().longValue());
			Assert.assertEquals(10001L + 2, resultList.get(1).getId().longValue());
			Assert.assertEquals(10001L + 4, resultList.get(2).getId().longValue());
			Assert.assertEquals(10001L + 9, resultList.get(3).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotInEmpty() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a inclure.
	 */
	@Test
	public void testWhereIn2200() {
		addNSuperHero(4500);

		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(2200, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a exclure.
	 */
	@Test
	public void testWhereNotIn2200() {
		addNSuperHero(4500);

		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = new TaskBuilder(taskDefinition)
					.withValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final TaskResult result = taskManager.execute(task);

			final DtList<SuperHero> resultList = result.getValue(DTC_SUPER_HERO_OUT);
			Assert.assertEquals(10 + 4500 - 2200, resultList.size());
		}
	}

	private static SuperHero createSuperHero(final long id) {
		final SuperHero superHero = new SuperHero();
		superHero.setId(id);
		return superHero;
	}

	private static TaskDefinition registerTaskWithNullableIn(final String taskDefinitionName, final String params) {
		final Domain doInteger = Home.getDefinitionSpace().resolve(DO_INTEGER, Domain.class);
		final Domain doSuperHeroList = Home.getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);

		return new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withInAttribute("PARAM_1", doInteger, true)
				.withInAttribute("PARAM_2", doInteger, false)
				.withInAttribute("PARAM_3", doInteger, false)
				.withOutAttribute(DTC_SUPER_HERO_OUT, doSuperHeroList, true)
				.build();
	}

	private static TaskDefinition registerTaskObject(final String taskDefinitionName, final String params) {
		final Domain doSupeHeroList = Home.getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);
		final Domain doSupeHero = Home.getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTO, Domain.class);

		return new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withInAttribute(DTO_SUPER_HERO, doSupeHero, true)
				.withOutAttribute(DTC_SUPER_HERO_OUT, doSupeHeroList, true)
				.build();
	}

	private static TaskDefinition registerTaskList(final String taskDefinitionName, final String params) {
		final Domain doSupeHeroList = Home.getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);

		return new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withInAttribute(DTC_SUPER_HERO_IN, doSupeHeroList, true)
				.withOutAttribute(DTC_SUPER_HERO_OUT, doSupeHeroList, true)
				.build();
	}

}
