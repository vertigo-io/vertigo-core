package io.vertigo.studio.plugins.reporting.task.metrics.join;

import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Plugin qui compte le nombre de jointures déclarées dans la requête.
 *
 * @author tchassagnette
 * @version $Id: JoinMetricEngine.java,v 1.4 2014/01/28 18:49:55 pchretien Exp $
 */
public final class JoinMetricEngine implements MetricEngine<TaskDefinition, JoinMetric> {
	/** {@inheritDoc} */
	public JoinMetric execute(final TaskDefinition taskDefinition) {
		Assertion.checkNotNull(taskDefinition);
		//---------------------------------------------------------------------
		final int joinCount = taskDefinition.getRequest().toUpperCase().split("JOIN").length - 1;
		final int fromCount = taskDefinition.getRequest().toUpperCase().split("FROM ").length - 1;
		return new JoinMetric(joinCount + fromCount);
	}
}
