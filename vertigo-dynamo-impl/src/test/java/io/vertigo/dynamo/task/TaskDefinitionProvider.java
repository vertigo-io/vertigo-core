package io.vertigo.dynamo.task;

import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_1;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_2;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_IN_INT_3;
import static io.vertigo.dynamo.task.TaskEngineMock.ATTR_OUT;

import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.util.ListBuilder;

public final class TaskDefinitionProvider extends SimpleDefinitionProvider {

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return new ListBuilder<Definition>()
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
