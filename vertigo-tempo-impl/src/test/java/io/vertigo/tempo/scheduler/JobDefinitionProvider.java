package io.vertigo.tempo.scheduler;

import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.tempo.job.metamodel.JobDefinition;
import io.vertigo.util.ListBuilder;

public final class JobDefinitionProvider implements DefinitionProvider {

	@Override
	public List<Definition> get() {
		return new ListBuilder()
				.add(new JobDefinition("JB_TEST", TestJob.class))
				.build();
	}

}
