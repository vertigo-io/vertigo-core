package io.vertigo.commons.health.data;

import io.vertigo.commons.health.HealthChecked;
import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.core.component.Component;
import io.vertigo.lang.VSystemException;

public class FailedComponentChecker implements Component {

	@HealthChecked(name = "failure")
	public HealthMeasure checkFails() {
		return HealthMeasure.builder()
				.withRedStatus("an error", new VSystemException("an error"))
				.build();
	}

}
