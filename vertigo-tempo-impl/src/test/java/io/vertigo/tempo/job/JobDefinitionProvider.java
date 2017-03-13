package io.vertigo.tempo.job;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.tempo.job.metamodel.JobDefinition;

public final class JobDefinitionProvider extends SimpleDefinitionProvider {
	private static JobDefinition createJobDefinition() {
		return new JobDefinition("JB_TEST_SYNC", TestJob.class);
	}

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(createJobDefinition());
	}

}
