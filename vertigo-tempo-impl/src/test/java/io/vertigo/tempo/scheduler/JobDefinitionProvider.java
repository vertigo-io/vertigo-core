package io.vertigo.tempo.scheduler;

import java.util.Collections;
import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionSupplier;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.tempo.job.metamodel.JobDefinition;

public final class JobDefinitionProvider implements DefinitionProvider {

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return Collections.singletonList((dS) -> new JobDefinition("JB_TEST", TestJob.class));
	}

}
