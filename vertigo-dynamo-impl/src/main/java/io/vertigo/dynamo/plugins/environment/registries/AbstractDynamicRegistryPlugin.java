package io.vertigo.dynamo.plugins.environment.registries;

import io.vertigo.dynamo.impl.environment.DynamicRegistryPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 * @param <G> Type de Grammaire
 */
public abstract class AbstractDynamicRegistryPlugin<G extends GrammarProvider> implements DynamicRegistryPlugin {
	private final G grammarProvider;

	/**
	 * Constructeur.
	 * @param grammarProvider Grammaire
	 */
	protected AbstractDynamicRegistryPlugin(final G grammarProvider) {
		Assertion.checkNotNull(grammarProvider);
		//---------------------------------------------------------------------
		this.grammarProvider = grammarProvider;
	}

	/** {@inheritDoc} */
	public final Grammar getGrammar() {
		return grammarProvider.getGrammar();
	}

	public final G getGrammarProvider() {
		return grammarProvider;
	}

	/** {@inheritDoc} */
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//Par défaut rien .
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param property Propriété
	 * @return Propriété de type Boolean uniquement
	 */
	protected static final Boolean getPropertyValueAsBoolean(final DynamicDefinition xdefinition, final EntityProperty property) {
		return (Boolean) xdefinition.getPropertyValue(property);
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param property Propriété
	 * @return Propriété de type String uniquement
	 */
	protected static final String getPropertyValueAsString(final DynamicDefinition xdefinition, final EntityProperty property) {
		return (String) xdefinition.getPropertyValue(property);
	}

}
