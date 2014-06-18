package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.dynamo.impl.environment.DynamicRegistry;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper pour les tests de regles sur les Definitions.
 * @author npiedeloup
 */
public final class DefinitionTestUtil implements DynamicRegistry {

	private DefinitionTestUtil() {
		//constructeur private
	}

	/**
	 * @return DynamicDefinitionRepository bouchon pour test 
	 */
	public static DynamicDefinitionRepository createDynamicDefinitionRepository() {
		return new DynamicDefinitionRepository(new DefinitionTestUtil());
	}

	public Grammar getGrammar() {
		return DomainGrammar.INSTANCE.getGrammar();
	}

	private final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();

	public void onDefinition(final DynamicDefinition definition) {
		dynamicDefinitions.add(definition);
	}

	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//rien
	}
}
