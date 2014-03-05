package io.vertigo.studio.plugins.reporting.domain.metrics.dependency;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.task.metamodel.TaskAttribute;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Comptage du nombre de champs.
 *
 * @author pchretien
 * @version $Id: DependencyMetricEngine.java,v 1.5 2014/01/28 18:49:55 pchretien Exp $
 */
public final class DependencyMetricEngine implements MetricEngine<DtDefinition, DependencyMetric> {

	/** {@inheritDoc} */
	public DependencyMetric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		int count = 0;
		for (final TaskDefinition taskDefinition : Home.getDefinitionSpace().getAll(TaskDefinition.class)) {
			for (final TaskAttribute taskAttribute : taskDefinition.getAttributes()) {
				if (!taskAttribute.getDomain().getDataType().isPrimitive()) {
					if (taskAttribute.getDomain().hasDtDefinition()) {
						if (dtDefinition.equals(taskAttribute.getDomain().getDtDefinition())) {
							count++;
						}
					}
				}

			}
		}
		return new DependencyMetric(count);
	}

}
