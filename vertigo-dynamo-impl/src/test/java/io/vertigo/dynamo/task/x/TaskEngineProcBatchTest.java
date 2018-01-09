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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineProcBatch;
import io.vertigo.dynamox.task.TaskEngineSelect;

/**
 *	TaskEngineProcBatch tests
 *
 * @author dszniten
 */
public final class TaskEngineProcBatchTest extends AbstractTestCaseJU4 {
	private static final String DTC_SUPER_HERO_IN = "DTC_SUPER_HERO_IN";
	private static final String SUPER_HERO_ID_LIST_IN = "SUPER_HERO_ID_LIST_IN";
	private static final String DTC_SUPER_HERO_OUT = "DTC_SUPER_HERO_OUT";
	private static final String OTHER_PARAM_IN = "OTHER_PARAM";
	private static final String DO_DT_SUPER_HERO_DTC = "DO_DT_SUPER_HERO_DTC";
	private static final String DO_LONGS = "DO_LONGS";
	private static final String DO_STRING = "DO_STRING";

	@Inject
	private TaskManager taskManager;
	@Inject
	private VTransactionManager transactionManager;

	private SuperHeroDataBase superHeroDataBase;

	@Override
	protected void doSetUp() throws Exception {
		superHeroDataBase = new SuperHeroDataBase(transactionManager, taskManager);
		superHeroDataBase.createDataBase();
	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatch() {
		final String request = new StringBuilder("insert into SUPER_HERO(ID, NAME) values (")
				.append("#").append(DTC_SUPER_HERO_IN + ".ID").append("# , ")
				.append("#").append(DTC_SUPER_HERO_IN + ".NAME").append("# ) ")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_TEST_INSERT_BATCH")
				.withEngine(TaskEngineProcBatch.class)
				.addInRequired(DTC_SUPER_HERO_IN, getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class))
				.withRequest(request)
				.build();

		final DtList<SuperHero> superHeroes = SuperHeroDataBase.getSuperHeroes();

		final Task task = Task.builder(taskDefinition)
				.addValue(DTC_SUPER_HERO_IN, superHeroes)
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assert.assertEquals(superHeroes.size(), selectHeroes().size());

	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatchWithAdditionalParam() {
		final String request = new StringBuilder("insert into SUPER_HERO(ID, NAME) values (")
				.append("#").append(DTC_SUPER_HERO_IN + ".ID").append("# , ")
				.append("#").append(OTHER_PARAM_IN).append("# ) ")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_TEST_INSERT_BATCH")
				.withEngine(TaskEngineProcBatch.class)
				.addInRequired(DTC_SUPER_HERO_IN, getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class))
				.addInRequired(OTHER_PARAM_IN, getApp().getDefinitionSpace().resolve(DO_STRING, Domain.class))
				.withRequest(request)
				.build();

		final DtList<SuperHero> superHeroes = SuperHeroDataBase.getSuperHeroes();

		final Task task = Task.builder(taskDefinition)
				.addValue(DTC_SUPER_HERO_IN, superHeroes)
				.addValue(OTHER_PARAM_IN, "test")
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assert.assertEquals(superHeroes.size(), selectHeroes().size());

	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatchPrimitive() {
		final String request = new StringBuilder("insert into SUPER_HERO(ID, NAME) values (")
				.append("#").append(SUPER_HERO_ID_LIST_IN).append("# , 'test' ").append(" )")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_TEST_INSERT_BATCH")
				.withEngine(TaskEngineProcBatch.class)
				.addInRequired(SUPER_HERO_ID_LIST_IN, getApp().getDefinitionSpace().resolve(DO_LONGS, Domain.class))
				.withRequest(request)
				.build();

		final List<Long> superHeroesIds = SuperHeroDataBase.getSuperHeroes().stream().map(superHero -> superHero.getId()).collect(Collectors.toList());

		final Task task = Task.builder(taskDefinition)
				.addValue(SUPER_HERO_ID_LIST_IN, superHeroesIds)
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assert.assertEquals(superHeroesIds.size(), selectHeroes().size());

	}

	private DtList<SuperHero> selectHeroes() {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_SELECT_HEROES")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from SUPER_HERO")
				.withOutRequired(DTC_SUPER_HERO_OUT, getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class))
				.build();
		final Task task = Task.builder(taskDefinition).build();
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			return taskManager.execute(task).getResult();
		}
	}

}
