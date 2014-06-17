package io.vertigo.studio.plugins.reporting.domain.metrics.fields;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.studio.reporting.MetricEngine;

/**
 * Comptage du nombre de champs.
 *
 * @author pchretien
 */
public final class FieldsMetricEngine implements MetricEngine<DtDefinition, FieldsMetric> {
	/** {@inheritDoc} */
	public FieldsMetric execute(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		final int size = dtDefinition.getFields().size();
		return new FieldsMetric(size);
	}
}
