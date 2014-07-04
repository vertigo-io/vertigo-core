package io.vertigo.dynamo.task;

import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.kernel.component.Manager;

/**
 * @author pchretien
 */
public interface TaskManager extends Manager {
	TaskResult execute(Task task);
}
