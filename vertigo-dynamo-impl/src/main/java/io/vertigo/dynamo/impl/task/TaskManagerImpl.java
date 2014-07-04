package io.vertigo.dynamo.impl.task;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

/**
 * @author  pchretien
 */
public final class TaskManagerImpl implements TaskManager {
	private final WorkManager workManager;

	@Inject
	public TaskManagerImpl(WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public TaskResult execute(Task task) {
		WorkItem<TaskResult, Task> workItem = new WorkItem<>(task, new WorkEngineProvider<>(task.getDefinition().getTaskEngineClass()));
		workManager.process(workItem);
		return workItem.getResult();
	}
}
