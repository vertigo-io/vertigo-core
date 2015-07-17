/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.impl.environment;

import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pchretien
 */
final class CompositeDynamicRegistry implements DynamicRegistry {
	private final List<DynamicRegistry> dynamicRegistries;
	private final Grammar grammar;
	private final List<DynamicDefinition> rootDynamicDefinitions;

	/**
	 * Constructor.
	 * @param handlerList Grammar handlers
	 */
	CompositeDynamicRegistry(final List<DynamicRegistryPlugin> handlerList) {
		Assertion.checkNotNull(handlerList);
		//-----
		dynamicRegistries = new ArrayList<DynamicRegistry>(handlerList);
		//Création de la grammaire.
		grammar = createGrammar();

		rootDynamicDefinitions = new ArrayList<>();
		for (final DynamicRegistry dynamicRegistry : dynamicRegistries) {
			rootDynamicDefinitions.addAll(dynamicRegistry.getRootDynamicDefinitions());
		}
	}

	private Grammar createGrammar() {
		final List<Entity> entities = new ArrayList<>();
		for (final DynamicRegistry dynamicRegistry : dynamicRegistries) {
			entities.addAll(dynamicRegistry.getGrammar().getEntities());
		}
		return new Grammar(entities);
	}

	/** {@inheritDoc} */
	@Override
	public Grammar getGrammar() {
		return grammar;
	}

	/** {@inheritDoc} */
	@Override
	public List<DynamicDefinition> getRootDynamicDefinitions() {
		return rootDynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//Les entités du noyaux ne sont pas à gérer per des managers spécifiques.
		if (KernelGrammar.GRAMMAR.getEntities().contains(xdefinition.getEntity())) {
			return;
		}
		final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
		dynamicRegistry.onNewDefinition(xdefinition, dynamicModelrepository);
	}

	/** {@inheritDoc} */
	@Override
	public Option<Definition> createDefinition(final DynamicDefinition xdefinition) {
		//Les entités du noyaux ne sont pas à gérer per des managers spécifiques.
		if (KernelGrammar.GRAMMAR.getEntities().contains(xdefinition.getEntity())) {
			return Option.none();
		}
		try {
			// perf: ifs ordonnés en gros par fréquence sur les projets
			final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
			return dynamicRegistry.createDefinition(xdefinition);
		} catch (final Exception e) {
			//on catch tout (notament les assertions) car c'est ici qu'on indique l'URI de la définition posant problème
			throw new RuntimeException("Erreur dans le traitement de " + xdefinition.getDefinitionKey().getName(), e);
		}
	}

	private DynamicRegistry lookUpDynamicRegistry(final DynamicDefinition xdefinition) {
		for (final DynamicRegistry dynamicRegistry : dynamicRegistries) {
			//On regarde si la grammaire contient la métaDefinition.
			if (dynamicRegistry.getGrammar().getEntities().contains(xdefinition.getEntity())) {
				return dynamicRegistry;
			}
		}
		//Si on n'a pas trouvé de définition c'est qu'il manque la registry.
		throw new IllegalArgumentException(xdefinition.getEntity().getName() + " " + xdefinition.getDefinitionKey().getName() + " non traitée. Il manque une DynamicRegistry ad hoc.");
	}
}
