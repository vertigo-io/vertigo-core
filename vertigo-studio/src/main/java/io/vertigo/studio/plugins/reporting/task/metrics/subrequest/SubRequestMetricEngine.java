package io.vertigo.studio.plugins.reporting.task.metrics.subrequest;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin qui compte le nombre de ss requete dans une requete SQL.
 * 
 * @author tchassagnette
 * @version $Id: SubRequestMetricEngine.java,v 1.4 2014/01/28 18:49:55 pchretien Exp $
 */
public final class SubRequestMetricEngine implements MetricEngine<TaskDefinition, SubRequestMetric> {
	/** {@inheritDoc} */
	public SubRequestMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		final int subRequestCount = taskDefinition.getRequest().toUpperCase().split("SELECT").length - 1;
		return new SubRequestMetric(subRequestCount);
	}
}
