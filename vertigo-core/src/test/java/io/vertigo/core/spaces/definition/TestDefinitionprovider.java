package io.vertigo.core.spaces.definition;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest.SampleDefinition;
import io.vertigo.lang.Assertion;

public class TestDefinitionprovider extends SimpleDefinitionProvider {

	@Inject
	public TestDefinitionprovider(@Named("testParam") final String testParam) {
		Assertion.checkArgNotEmpty(testParam);

	}

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new SampleDefinition());
	}

	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		// we do nothing
	}

}
