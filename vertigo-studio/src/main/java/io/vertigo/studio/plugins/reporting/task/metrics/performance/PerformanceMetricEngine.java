package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin de calcul du temps d'exécution d'une requête.
 * 
 * @author tchassagnette
 */
public final class PerformanceMetricEngine implements MetricEngine<TaskDefinition, PerformanceMetric> {
	private final TaskManager taskManager;

	/**
	 * Constructeur apr défaut.
	 * @param workManager Manager des works
	 */
	public PerformanceMetricEngine(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//---------------------------------------------------------------------
		this.taskManager = taskManager;
	}

	/** {@inheritDoc} */
	public PerformanceMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		try {
			return doExecute(taskDefinition);
		} catch (final Throwable e) {
			//throw new RiException("Erreur du plugin perfs", e);
			return new PerformanceMetric(e);
		}

	}

	private PerformanceMetric doExecute(final TaskDefinition taskDefinition) {
		//System.out.println(">>>>" + currentTask.getEngineClass().getCanonicalName());
		if (TaskEngineSelect.class.isAssignableFrom(taskDefinition.getTaskEngineClass()) && !hasNotNullOutParams(taskDefinition)) {
			//	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>" + currentTask.getEngineClass().getCanonicalName());
			final TaskPopulator taskPopulator = new TaskPopulator(taskDefinition);
			final Task task = taskPopulator.populateTask();
			final long startTime = System.currentTimeMillis();
			/*TaskResult result =*/taskManager.execute(task);
			//on n'utilise pas le resultat
			final long endTime = System.currentTimeMillis();
			final long executionTime = endTime - startTime;
			return new PerformanceMetric(executionTime);
		}
		//Le test n'a pas de sens. 
		return new PerformanceMetric();
	}

	private static boolean hasNotNullOutParams(final TaskDefinition taskDefinition) {
		for (final TaskAttribute attribute : taskDefinition.getAttributes()) {
			if (!attribute.isIn() && attribute.isNotNull()) {
				return true;
			}
		}
		return false;
	}
}
