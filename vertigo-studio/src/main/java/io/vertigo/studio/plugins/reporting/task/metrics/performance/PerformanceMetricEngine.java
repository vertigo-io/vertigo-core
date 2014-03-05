package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin de calcul du temps d'exécution d'une requête.
 * 
 * @author tchassagnette
 * @version $Id: PerformanceMetricEngine.java,v 1.6 2014/01/28 18:49:55 pchretien Exp $
 */
public final class PerformanceMetricEngine implements MetricEngine<TaskDefinition, PerformanceMetric> {
	private final WorkManager workManager;

	/**
	 * Constructeur apr défaut.
	 * @param workManager Manager des works
	 */
	public PerformanceMetricEngine(final WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
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
		if (TaskEngineSelect.class.isAssignableFrom(getTaskEngineClass(taskDefinition)) && !hasNotNullOutParams(taskDefinition)) {
			//	System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>" + currentTask.getEngineClass().getCanonicalName());
			final TaskPopulator taskPopulator = new TaskPopulator(taskDefinition);
			final Task task = taskPopulator.populateTask();
			final long startTime = System.currentTimeMillis();
			workManager.process(task, taskDefinition.getTaskEngineProvider());
			final long endTime = System.currentTimeMillis();
			final long executionTime = endTime - startTime;
			return new PerformanceMetric(executionTime);
		}
		//Le test n'a pas de sens. 
		return new PerformanceMetric();
	}

	private Class<? extends TaskEngine> getTaskEngineClass(final TaskDefinition taskDefinition) {
		return (Class<? extends TaskEngine>) ClassUtil.classForName(taskDefinition.getTaskEngineProvider().getName());
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
