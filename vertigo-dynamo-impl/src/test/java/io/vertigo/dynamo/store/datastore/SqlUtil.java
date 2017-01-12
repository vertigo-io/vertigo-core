package io.vertigo.dynamo.store.datastore;

import java.util.List;
import java.util.Optional;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamox.task.TaskEngineProc;

public final class SqlUtil {
	public static void execRequests(
			final VTransactionManager transactionManager,
			final TaskManager taskManager,
			final List<String> requests,
			final String taskName,
			final Optional<String> optDataSpace) {
		//A chaque test on recrée la table famille
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final String request : requests) {
				final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
						.withEngine(TaskEngineProc.class)
						.withRequest(request)
						.withDataSpace(optDataSpace.orElse(null))
						.build();
				final Task task = new TaskBuilder(taskDefinition).build();
				taskManager.execute(task);
			}
		}
	}
}
