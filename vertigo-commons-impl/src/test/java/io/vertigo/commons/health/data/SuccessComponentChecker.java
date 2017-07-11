package io.vertigo.commons.health.data;

import io.vertigo.commons.health.HealthChecked;
import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.core.component.Component;

public class SuccessComponentChecker implements Component {

	@HealthChecked(name = "success")
	public HealthMeasure checkSuccess() {
		return HealthMeasure
				.builder()
				.withGreenStatus(null)
				.build();
	}

}
