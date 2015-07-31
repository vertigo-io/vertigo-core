package io.vertigo.core.environment;

import io.vertigo.core.impl.environment.DynamicRegistry;
import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock pour les tests de regles sur les Definitions.
 * @author npiedeloup
 */
public final class DslDynamicRegistryMock implements DynamicRegistry {

	private DslDynamicRegistryMock() {
		//constructeur private
	}

	/**
	 * @return DynamicDefinitionRepository bouchon pour test
	 */
	public static DynamicDefinitionRepository createDynamicDefinitionRepository() {
		return new DynamicDefinitionRepository(new DslDynamicRegistryMock());
	}

	@Override
	public Grammar getGrammar() {
		return PersonnGrammar.GRAMMAR;
	}

	private final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();

	@Override
	public Option<Definition> createDefinition(final DynamicDefinition definition) {
		dynamicDefinitions.add(definition);
		return Option.none();
	}

	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//
	}

	@Override
	public List<DynamicDefinition> getRootDynamicDefinitions() {
		return Collections.emptyList();
	}
}
