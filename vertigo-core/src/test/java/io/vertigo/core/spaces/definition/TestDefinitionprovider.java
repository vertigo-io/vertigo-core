package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionSupplier;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;

public class TestDefinitionprovider implements DefinitionProvider {

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return Collections.singletonList((dS) -> new SampleDefinition());
	}

}
