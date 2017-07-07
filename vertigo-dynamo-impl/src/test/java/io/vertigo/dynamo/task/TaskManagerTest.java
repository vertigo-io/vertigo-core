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
package io.vertigo.dynamo.task;

import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_1;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_2;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_3;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;

/**
 *
 * @author dchallas
 */
public final class TaskManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private TaskManager taskManager;

	/**
	 * Test l'enregistrement d'une task.
	 */
	@Test
	public void testRegistry() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final TaskDefinition taskDefinition = definitionSpace.resolve("TK_ADD", TaskDefinition.class);
		Assert.assertNotNull(taskDefinition);
	}

	/**
	 * Test de récupération d'une task non enregistrée.
	 */
	@Test(expected = NullPointerException.class)
	public void testRegistryWithNull() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		//L'appel à la résolution doit remonter une assertion
		final TaskDefinition taskDefinition = definitionSpace.resolve(null, TaskDefinition.class);
		nop(taskDefinition);
	}

	/***
	 * Test nominal d'une addition.
	 */
	@Test
	public void testExecuteAdd() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final TaskDefinition taskDefinition = definitionSpace.resolve("TK_ADD", TaskDefinition.class);
		Assert.assertEquals(Integer.valueOf(10), executeTask(taskDefinition, 5, 2, 3));
	}

	/***
	 * Test nominal d'une multiplication.
	 */
	@Test
	public void testExecuteMulti() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final TaskDefinition taskDefinition = definitionSpace.resolve("TK_MULTI", TaskDefinition.class);

		Assert.assertEquals(Integer.valueOf(30), executeTask(taskDefinition, 5, 2, 3));
	}

	/**
	 * On vérifie les caractères obligatoires des attributs en entrée.
	 */
	@Test(expected = NullPointerException.class)
	public void testExecuteNull() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final TaskDefinition taskDefinition = definitionSpace.resolve("TK_MULTI", TaskDefinition.class);
		//on vérifie que le passage d'un paramètre null déclenche une assertion
		executeTask(taskDefinition, null, 2, 3);
	}

	/**
	 * Test de double exécution d'une tache.
	 */
	@Test
	public void testExecuteAddAdd() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final TaskDefinition taskDefinition = definitionSpace.resolve("TK_ADD", TaskDefinition.class);

		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, 1)
				.addValue(ATTR_IN_INT_2, 8)
				.addValue(ATTR_IN_INT_3, 7)
				.build();

		final Integer result1 = taskManager
				.execute(task)
				.getResult();

		Assert.assertEquals(Integer.valueOf(16), result1);

		final Integer result2 = taskManager
				.execute(task)
				.getResult();

		Assert.assertEquals(Integer.valueOf(16), result2);
	}

	/**
	 * @param value1 entier 1
	 * @param value2 entier 2
	 * @param value3 entier 3
	 * @return somme des entiers.
	 */
	private Integer executeTask(final TaskDefinition taskDefinition, final Integer value1, final Integer value2, final Integer value3) {
		final Task task = Task.builder(taskDefinition)
				.addValue(ATTR_IN_INT_1, value1)
				.addValue(ATTR_IN_INT_2, value2)
				.addValue(ATTR_IN_INT_3, value3)
				.build();

		return taskManager
				.execute(task)
				.getResult();
	}

}
