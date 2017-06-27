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
package io.vertigo.dynamo.task.x;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.Home;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineProcBatch;
import io.vertigo.dynamox.task.TaskEngineSelect;

/**
 *	TaskEngineProcBatch tests
 *
 * @author dszniten
 */
public final class TaskEngineProcBatchTest extends AbstractTestCaseJU4 {
	private static final String DTC_SUPER_HERO_IN = "DTC_SUPER_HERO_IN";
	private static final String DTC_SUPER_HERO_OUT = "DTC_SUPER_HERO_OUT";
	private static final String DO_DT_SUPER_HERO_DTC = "DO_DT_SUPER_HERO_DTC";

	@Inject
	private TaskManager taskManager;
	@Inject
	private VTransactionManager transactionManager;

	@Override
	protected void doSetUp() throws Exception {
		//A chaque test on recrée la table SUPER_HERO
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			execStatement("create table SUPER_HERO(id BIGINT , name varchar(255));");
			execStatement("create sequence SEQ_SUPER_HERO start with 10001 increment by 1");
		}
	}

	private void execStatement(final String request) {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_INIT")
				.withEngine(TaskEngineProc.class)
				.withRequest(request)
				.build();
		final Task task = Task.builder(taskDefinition).build();
		taskManager.execute(task);
	}

	/**
	 * Tests batch insertion with a task
	 */
	@Test
	public void testInsertBatch() {
		final String request = new StringBuilder("insert into SUPER_HERO(ID, NAME) values (")
				.append("#").append(DTC_SUPER_HERO_IN + ".0.ID").append("# , ")
				.append("#").append(DTC_SUPER_HERO_IN + ".0.NAME").append("# ) ")
				.toString();
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_TEST_INSERT_BATCH")
				.withEngine(TaskEngineProcBatch.class)
				.addInRequired(DTC_SUPER_HERO_IN, Home.getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class))
				.withRequest(request)
				.build();

		final DtList<SuperHero> superHeroes = getSuperHeroes();

		final Task task = Task.builder(taskDefinition)
				.addValue(DTC_SUPER_HERO_IN, superHeroes)
				.build();

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			taskManager.execute(task);
			transaction.commit();
		}

		Assert.assertEquals(superHeroes.size(), selectHeroes().size());

	}

	private DtList<SuperHero> getSuperHeroes() {
		return DtList.<SuperHero> of(
				createSuperHero(1, "superman"),
				createSuperHero(2, "batman"),
				createSuperHero(3, "catwoman"),
				createSuperHero(4, "wonderwoman"),
				createSuperHero(5, "aquaman"),
				createSuperHero(6, "green lantern"),
				createSuperHero(7, "captain america"),
				createSuperHero(8, "spiderman"));
	}

	private SuperHero createSuperHero(final long id, final String name) {
		final SuperHero superHero = new SuperHero();
		superHero.setId(id);
		superHero.setName(name);
		return superHero;
	}

	private DtList<SuperHero> selectHeroes() {
		final TaskDefinition taskDefinition = TaskDefinition.builder("TK_SELECT_HEROES")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from SUPER_HERO")
				.withOutRequired(DTC_SUPER_HERO_OUT, Home.getApp().getDefinitionSpace().resolve(DO_DT_SUPER_HERO_DTC, Domain.class))
				.build();
		final Task task = Task.builder(taskDefinition).build();
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			return taskManager.execute(task).getResult();
		}

	}

}
