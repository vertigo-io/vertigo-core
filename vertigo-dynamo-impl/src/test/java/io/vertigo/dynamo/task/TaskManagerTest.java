package io.vertigo.dynamo.task;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.Home;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author dchallas
 * $Id: TaskManagerTest.java,v 1.7 2014/01/20 18:58:20 pchretien Exp $
 */
public final class TaskManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private WorkManager workManager;

	/**
	 * test la description du manager.
	 */
	@Test
	public void testDescription() {
		final String TK_ADD_DESC = "TK_ADD_DESC";
		registerTask(TK_ADD_DESC, "+");
	}

	/**
	 * Test l'enregistrement d'une task.
	 */
	@Test
	public void testRegistry() {
		final String TK_ADD = "TK_ADD";

		registerTask(TK_ADD, "+");
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_ADD, TaskDefinition.class);
		Assert.assertNotNull(taskDefinition);
	}

	/**
	 * Test de récupération d'une task non enregistrée.
	 */
	@Test(expected = NullPointerException.class)
	public void testRegistryWithNull() {
		//On ne respect pas le pattern TK_
		final String TK_ADD_REGISTRY = "TK_ADD_REGISTRY";
		registerTask(TK_ADD_REGISTRY, "+");

		final String id = null;
		//L'appel à la résolution doit remonter une assertion 
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(id, TaskDefinition.class);
		nop(taskDefinition);
	}

	/**
	 * Test l'enregistrement d'une task avec une faute de nommage.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testRegistryWithError() {
		//On ne respect pas le pattern TK_
		final String TK_ADD_REGISTRY = "TZ_ADD_REGISTRY";

		registerTask(TK_ADD_REGISTRY, "+");
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_ADD_REGISTRY, TaskDefinition.class);
		nop(taskDefinition);
	}

	/***
	 * Test nominal d'une addition.
	 */
	@Test
	public void testExecuteAdd() {
		final String TK_ADD = "TK_ADD";
		registerTask(TK_ADD, "+");
		Assert.assertEquals(Integer.valueOf(10), executeTask(TK_ADD, 5, 2, 3));
	}

	/***
	 * Test nominal d'une multiplication.
	 */
	@Test
	public void testExecuteMulti() {
		final String TK_MULTI = "TK_MULTI";
		registerTask(TK_MULTI, "*");
		Assert.assertEquals(Integer.valueOf(30), executeTask(TK_MULTI, 5, 2, 3));
	}

	/**
	 * On vérifie les caractères obligatoires des attributs en entrée.
	 * @throws Exception erreur
	 */
	@Test(expected = NullPointerException.class)
	public void testExecuteNull() {
		final String TK_MULTI_2 = "TK_MULTI_2";
		registerTask(TK_MULTI_2, "*");
		//on vérifie que le passage d'un paramètre null déclenche une assertion
		executeTask(TK_MULTI_2, null, 2, 3);
	}

	/**
	 * Vérification de l'impossibilité d'enregistrer deux fois une tache. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testDoubleRegister() {
		final String TK_MULTI_3 = "TK_MULTI_3";

		registerTask(TK_MULTI_3, "*");
		//On déclenche une assertion en réenregistrant la même tache
		registerTask(TK_MULTI_3, "*");
	}

	/**
	 * Test de double exécution d'une tache.
	 */
	@Test
	public void testExecuteAddAdd() {
		final String TK_ADD_2 = "TK_ADD_2";
		registerTask(TK_ADD_2, "+");
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(TK_ADD_2, TaskDefinition.class);

		final Task task = new TaskBuilder(taskDefinition)//
				.withValue(TaskEngineMock.ATTR_IN_INT_1, 1)//
				.withValue(TaskEngineMock.ATTR_IN_INT_2, 8)//
				.withValue(TaskEngineMock.ATTR_IN_INT_3, 7)//
				.build();

		// on suppose un appel synchrone : getResult immédiat.
		TaskResult result;

		result = workManager.<TaskResult, Task> process(task, taskDefinition.getTaskEngineProvider());
		Assert.assertEquals(Integer.valueOf(16), result.getValue(TaskEngineMock.ATTR_OUT));

		result = workManager.<TaskResult, Task> process(task, taskDefinition.getTaskEngineProvider());
		Assert.assertEquals(Integer.valueOf(16), result.getValue(TaskEngineMock.ATTR_OUT));

	}

	private TaskDefinition registerTask(final String taskDefinitionName, final String params) {
		final Domain doInteger = Home.getDefinitionSpace().resolve("DO_INTEGER", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskDefinitionName)//
				.withEngine(TaskEngineMock.class)//
				.withRequest(params)//
				.withPackageName(TaskEngineMock.class.getPackage().getName())//
				.withAttribute(TaskEngineMock.ATTR_IN_INT_1, doInteger, true, true)//
				.withAttribute(TaskEngineMock.ATTR_IN_INT_2, doInteger, true, true)//
				.withAttribute(TaskEngineMock.ATTR_IN_INT_3, doInteger, true, true)//
				.withAttribute(TaskEngineMock.ATTR_OUT, doInteger, true, false)//
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	/**  
	 * @param value1 entier 1
	 * @param value2 entier 2
	 * @param value3 entier 3
	 * @return somme des entiers.
	 */
	private Integer executeTask(final String taskDefinitionName, final Integer value1, final Integer value2, final Integer value3) {
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(taskDefinitionName, TaskDefinition.class);
		final Task task = new TaskBuilder(taskDefinition)//
				.withValue(TaskEngineMock.ATTR_IN_INT_1, value1)//
				.withValue(TaskEngineMock.ATTR_IN_INT_2, value2)//
				.withValue(TaskEngineMock.ATTR_IN_INT_3, value3)//
				.build();
		// on suppose un appel synchrone : getResult immédiat
		final TaskResult result = workManager.<TaskResult, Task> process(task, taskDefinition.getTaskEngineProvider());
		//final WorkItem<Task, TaskResult> workItem = workManager.<Task, TaskResult> schedule(task);
		//workManager.waitForAll(java.util.Collections.singleton(workItem), 10 * 1000);
		//final TaskResult result = workItem.getResult();
		return result.getValue(TaskEngineMock.ATTR_OUT);
	}
}
