package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;

public class TestDefinitionprovider extends SimpleDefinitionProvider {

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new SampleDefinition());
	}

}
