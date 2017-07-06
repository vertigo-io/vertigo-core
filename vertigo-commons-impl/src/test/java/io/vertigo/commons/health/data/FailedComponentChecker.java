package io.vertigo.commons.health.data;

import java.util.Collections;
import java.util.List;

import io.vertigo.commons.health.HealthComponentStatusSupplier;
import io.vertigo.commons.health.HealthControlPoint;
import io.vertigo.core.component.Component;
import io.vertigo.lang.VSystemException;

public class FailedComponentChecker implements Component, HealthComponentStatusSupplier {

	@Override
	public List<HealthControlPoint> getControlPoints() {
		return Collections.singletonList(
				HealthControlPoint
						.of(this.getClass().getSimpleName())
						.withRedStatus("an error", new VSystemException("an error"))
						.build());
	}

}
