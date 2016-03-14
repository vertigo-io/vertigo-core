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
package io.vertigo.core.definition.loader;

import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.dynamic.DynamicRegistry;
import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pchretien
 */
final class CompositeDynamicRegistry implements DynamicRegistry {
	private final List<DynamicRegistry> dynamicRegistries;
	private final EntityGrammar grammar;
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

	private EntityGrammar createGrammar() {
		final List<Entity> entities = new ArrayList<>();
		for (final DynamicRegistry dynamicRegistry : dynamicRegistries) {
			entities.addAll(dynamicRegistry.getGrammar().getEntities());
		}
		return new EntityGrammar(entities);
	}

	/** {@inheritDoc} */
	@Override
	public EntityGrammar getGrammar() {
		return grammar;
	}

	/** {@inheritDoc} */
	@Override
	public List<DynamicDefinition> getRootDynamicDefinitions() {
		return rootDynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public List<Definition> createDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdefinition) {
		//Les entités du noyaux ne sont pas à gérer per des managers spécifiques.
		if (KernelGrammar.GRAMMAR.getEntities().contains(xdefinition.getEntity())) {
			return Collections.emptyList();
		}
		try {
			// perf: ifs ordonnés en gros par fréquence sur les projets
			final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
			return dynamicRegistry.createDefinition(definitionSpace, xdefinition);
		} catch (final Exception e) {
			//on catch tout (notament les assertions) car c'est ici qu'on indique l'URI de la définition posant problème
			throw new WrappedException("An error occurred during the creation of the following definition : " + xdefinition.getName(), e);
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
		throw new IllegalArgumentException(xdefinition.getEntity().getName() + " " + xdefinition.getName() + " non traitée. Il manque une DynamicRegistry ad hoc.");
	}
}
