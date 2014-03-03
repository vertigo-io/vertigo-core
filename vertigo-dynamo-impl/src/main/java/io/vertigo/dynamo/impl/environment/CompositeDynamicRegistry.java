package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

final class CompositeDynamicRegistry implements DynamicRegistry {
	private final List<DynamicRegistry> handlerList;
	private final Grammar grammar;

	/**
	 * Constructeur.
	 */
	CompositeDynamicRegistry(final List<DynamicRegistryPlugin> handlerList) {
		Assertion.checkNotNull(handlerList);
		//---------------------------------------------------------------------
		this.handlerList = new ArrayList<DynamicRegistry>(handlerList);
		//Création de la grammaire.
		grammar = createGrammar();
	}

	private Grammar createGrammar() {
		final List<Grammar> grammars = new ArrayList<>();
		for (final DynamicRegistry dynamicRegistry : handlerList) {
			grammars.add(dynamicRegistry.getGrammar());
		}
		return new Grammar(grammars);
	}

	/** {@inheritDoc} */
	public Grammar getGrammar() {
		return grammar;
	}

	/** {@inheritDoc} */
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//Les entités du noyaux ne sont pas à gérer per des managers spécifiques.
		if (KernelGrammar.INSTANCE.getGrammar().getEntities().contains(xdefinition.getEntity())) {
			return;
		}
		final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
		dynamicRegistry.onNewDefinition(xdefinition, dynamicModelrepository);
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		//Les entités du noyaux ne sont pas à gérer per des managers spécifiques.
		if (KernelGrammar.INSTANCE.getGrammar().getEntities().contains(xdefinition.getEntity())) {
			return;
		}
		try {
			//final DynamicMetaDefinition metaDefinition = xdefinition.getMetaDefinition();
			// perf: ifs ordonnés en gros par fréquence sur les projets
			final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
			dynamicRegistry.onDefinition(xdefinition);
		} catch (final Throwable e) {
			//on catch tout (notament les assertions) car c'est ici qu'on indique l'URI de la définition posant problème
			throw new VRuntimeException("Erreur dans le traitement de " + xdefinition.getDefinitionKey().getName(), e);
		}
	}

	private DynamicRegistry lookUpDynamicRegistry(final DynamicDefinition xdefinition) {
		for (final DynamicRegistry dynamicRegistry : handlerList) {
			//On regarde si la grammaire contient la métaDefinition.
			if (dynamicRegistry.getGrammar().getEntities().contains(xdefinition.getEntity())) {
				return dynamicRegistry;
			}
		}
		//Si on n'a pas trouvé de définition c'est qu'il manque la registry.
		throw new IllegalArgumentException(xdefinition.getEntity().getName() + " " + xdefinition.getDefinitionKey().getName() + " non traitée. Il manque une DynamicRegistry ad hoc.");
	}
}
