package io.vertigo.tempo.job;

import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.tempo.job.metamodel.JobDefinition;
import io.vertigo.util.ListBuilder;

public final class JobDefinitionProvider implements DefinitionProvider {
	private static JobDefinition createJobDefinition() {
		return new JobDefinition("JB_TEST_SYNC", TestJob.class);
	}

	@Override
	public List<Definition> get() {
		return new ListBuilder()
				.add(createJobDefinition())
				.build();
	}

}
