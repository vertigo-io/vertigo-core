/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineSelect;

/**
 *
 * @author npiedeloup
 */
public final class TaskEngineSelectDynamicTest extends AbstractTestCaseJU5 {
	private static final String DTC_SUPER_HERO_IN = "DTC_SUPER_HERO_IN";
	private static final String SUPER_HERO_ID_LIST = "SUPER_HERO_ID_LIST";
	private static final String DO_INTEGER = "DO_INTEGER";
	private static final String DO_LONGS = "DO_LONGS";
	private static final String DO_DT_SUPER_HERO_DTO = "DO_DT_SUPER_HERO_DTO";
	private static final String DO_DT_SUPER_HERO_DTC = "DO_DT_SUPER_HERO_DTC";
	private static final String DTO_SUPER_HERO = "DTO_SUPER_HERO";
	@Inject
	private TaskManager taskManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;

	private SuperHeroDataBase superHeroDataBase;

	@Override
	protected void doSetUp() throws Exception {
		superHeroDataBase = new SuperHeroDataBase(transactionManager, taskManager);
		superHeroDataBase.createDataBase();
		superHeroDataBase.populateSuperHero(storeManager, 10);
	}

	/**
	 * Test de double exécution d'une tache.
	 */
	@Test
	public void testScript() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(false) {%>where ID = #DTO_SUPER_HERO.ID#<%}%>");

			final SuperHero superHero = createSuperHero(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVar() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(dtoSuperHero.getId() == 10002L) {%>where ID = #DTO_SUPER_HERO.ID#<%}%>");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(1, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
		}
	}

	/**
	 * Test des nullable.
	 */
	@Test
	public void testNullable() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskWithNullableIn("TK_NULLABLE_TEST",
					"select * from SUPER_HERO where ID = #PARAM_1#<%if(param2!=null) {%> OR ID = #PARAM_2#+2 <%}%><%if(param3!=null) {%> OR ID = #PARAM_3#+3<%}%>");

			final Task task = Task.builder(taskDefinition)
					.addValue("PARAM_1", 10002)
					.addValue("PARAM_2", null)
					.addValue("PARAM_3", 10002)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10002L, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10002L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test des scripts.
	 */
	@Test
	public void testScriptVarList() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_SCRIPT_TEST",
					"select * from SUPER_HERO <%if(!dtcSuperHeroIn.isEmpty()) {%>where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)<%}%>");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test du preprocessor trim.
	 * Note: nous n'avons pas accès à la chaine trimée, on check juste que la requete est valide.
	 */
	@Test
	public void testTrim() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskObject("TK_SCRIPT_TEST",
					"select * from SUPER_HERO  \n<%if(false) {%>\nwhere ID = #DTO_SUPER_HERO.ID#\n<%}%>\n");

			final SuperHero superHero = new SuperHero();
			superHero.setId(10001L + 1);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTO_SUPER_HERO, superHero)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();
			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereIn() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInPrimitive() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskListPrimitive("TK_WHERE_IN_PRIMITIVE_TEST",
					"select * from SUPER_HERO  where ID in (#SUPER_HERO_ID_LIST.ROWNUM#)");

			final List<Long> ids = Arrays.asList(10001L + 1, 10001L + 3);

			final Task task = Task.builder(taskDefinition)
					.addValue(SUPER_HERO_ID_LIST, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInTab() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where\tID in\t(#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInParenthesis() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where\t(ID in\t(#DTC_SUPER_HERO_IN.ROWNUM.ID#))");

			final DtList<SuperHero> ids = DtList.of(createSuperHero(10001L + 1), createSuperHero(10001L + 3));

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2, resultList.size());
			Assertions.assertEquals(10001L + 1, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 3, resultList.get(1).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereInEmpty() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(0, resultList.size());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotIn() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids).build();

			ids.add(createSuperHero(10001L + 1));
			ids.add(createSuperHero(10001L + 3));
			ids.add(createSuperHero(10001L + 5));
			ids.add(createSuperHero(10001L + 6));
			ids.add(createSuperHero(10001L + 7));
			ids.add(createSuperHero(10001L + 8));

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(4, resultList.size());
			Assertions.assertEquals(10001L + 0, resultList.get(0).getId().longValue());
			Assertions.assertEquals(10001L + 2, resultList.get(1).getId().longValue());
			Assertions.assertEquals(10001L + 4, resultList.get(2).getId().longValue());
			Assertions.assertEquals(10001L + 9, resultList.get(3).getId().longValue());
		}
	}

	/**
	 * Test exécution d'une tache.
	 */
	@Test
	public void testWhereNotInEmpty() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a inclure.
	 */
	@Test
	public void testWhereIn2200() {
		superHeroDataBase.populateSuperHero(storeManager, 4500);
		//---

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(2200, resultList.size());
		}
	}

	/**
	 * Test where in avec 2200 Id a exclure.
	 */
	@Test
	public void testWhereNotIn2200() {
		superHeroDataBase.populateSuperHero(storeManager, 4500);
		//---

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final TaskDefinition taskDefinition = registerTaskList("TK_WHERE_ID_TEST",
					"select * from SUPER_HERO  where ID not in (#DTC_SUPER_HERO_IN.ROWNUM.ID#)");

			final DtList<SuperHero> ids = new DtList<>(SuperHero.class);
			for (int i = 0; i < 2200; i++) {
				ids.add(createSuperHero(10001L + 2 * i));
			}

			final Task task = Task.builder(taskDefinition)
					.addValue(DTC_SUPER_HERO_IN, ids)
					.build();

			final DtList<SuperHero> resultList = taskManager
					.execute(task)
					.getResult();

			Assertions.assertEquals(10 + 4500 - 2200, resultList.size());
		}
	}

	private static SuperHero createSuperHero(final long id) {
		final SuperHero superHero = new SuperHero();
		superHero.setId(id);
		return superHero;
	}

	private TaskDefinition registerTaskWithNullableIn(final String taskDefinitionName, final String params) {
		final Domain doInteger = getApp().getDefinitionSpace().resolve(DO_INTEGER, Domain.class);
		final Domain doSuperHeroes = getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInRequired("PARAM_1", doInteger)
				.addInOptional("PARAM_2", doInteger)
				.addInOptional("PARAM_3", doInteger)
				.withOutRequired("dtc", doSuperHeroes)
				.build();
	}

	private TaskDefinition registerTaskObject(final String taskDefinitionName, final String params) {
		final Domain doSupeHeroes = getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);
		final Domain doSupeHero = getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTO, Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInRequired(DTO_SUPER_HERO, doSupeHero)
				.withOutRequired("dtc", doSupeHeroes)
				.build();
	}

	private TaskDefinition registerTaskList(final String taskDefinitionName, final String params) {
		final Domain doSupeHeroes = getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInRequired(DTC_SUPER_HERO_IN, doSupeHeroes)
				.withOutRequired("dtc", doSupeHeroes)
				.build();
	}

	private TaskDefinition registerTaskListPrimitive(final String taskDefinitionName, final String params) {
		final Domain doLongs = getApp().getDefinitionSpace().resolve(DO_LONGS, Domain.class);
		final Domain doSupeHeroes = getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class);

		return TaskDefinition.builder(taskDefinitionName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(params)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.addInRequired(SUPER_HERO_ID_LIST, doLongs)
				.withOutRequired("dtc", doSupeHeroes)
				.build();
	}

}
