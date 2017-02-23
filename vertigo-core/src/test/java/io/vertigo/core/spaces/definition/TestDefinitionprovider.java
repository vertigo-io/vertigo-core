package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;

public class TestDefinitionprovider implements DefinitionProvider {

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return Collections.singletonList((dS) -> new SampleDefinition());
	}

}
