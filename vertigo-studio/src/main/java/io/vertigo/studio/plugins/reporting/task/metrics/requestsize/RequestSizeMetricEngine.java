package io.vertigo.studio.plugins.reporting.task.metrics.requestsize;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin de calcul de la taille en caractères d'une requête.
 * 
 * @author tchassagnette
 * @version $Id: RequestSizeMetricEngine.java,v 1.4 2014/01/28 18:49:55 pchretien Exp $
 */
public final class RequestSizeMetricEngine implements MetricEngine<TaskDefinition, RequestSizeMetric> {
	/** {@inheritDoc} */
	public RequestSizeMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		final int size = taskDefinition.getRequest().length();
		return new RequestSizeMetric(size);
	}
}
