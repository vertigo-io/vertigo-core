package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;
import io.vertigo.core.spaces.definiton.Definition;

public class TestDefinitionprovider implements DefinitionProvider {

	@Override
	public List<Definition> get() {
		return Collections.singletonList(new SampleDefinition());
	}

}
