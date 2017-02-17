package io.vertigo.dynamo.task;

import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_1;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_2;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_3;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_OUT;

import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.util.ListBuilder;

public final class TaskDefinitionProvider implements DefinitionProvider {

	@Override
	public List<Definition> get() {
		return new ListBuilder()
				.add(buildTaskDefinition("TK_MULTI", "*"))
				.add(buildTaskDefinition("TK_ADD", "+"))
				.build();
	}

	private TaskDefinition buildTaskDefinition(final String taskDefinitionName, final String params) {
		final DefinitionSpace definitionSpace = Home.getApp().getDefinitionSpace();
		final Domain doInteger = definitionSpace.resolve("DO_INTEGER", Domain.class);

		return new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(TaskEngineMock.class)
				.withRequest(params)
				.withPackageName(TaskEngineMock.class.getPackage().getName())
				.addInRequired(ATTR_IN_INT_1, doInteger)
				.addInRequired(ATTR_IN_INT_2, doInteger)
				.addInRequired(ATTR_IN_INT_3, doInteger)
				.withOutRequired(ATTR_OUT, doInteger)
				.build();
	}
}
