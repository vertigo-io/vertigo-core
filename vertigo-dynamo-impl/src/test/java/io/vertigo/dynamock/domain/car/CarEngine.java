package io.vertigo.dynamock.domain.car;

import io.vertigo.dynamo.task.model.TaskEngine;

public class CarEngine extends TaskEngine {

	@Override
	protected void execute() {
		System.out.println(this.getTaskDefinition().getRequest());
	}
}
