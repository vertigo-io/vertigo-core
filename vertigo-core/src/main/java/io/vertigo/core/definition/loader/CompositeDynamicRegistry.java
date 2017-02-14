/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.definition.dsl.dynamic.DynamicRegistry;
import io.vertigo.core.definition.dsl.entity.DslGrammar;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * @author pchretien
 */
final class CompositeDynamicRegistry implements DynamicRegistry {
	private final List<DynamicRegistry> dynamicRegistries;
	private final DslGrammar grammar;
	private final List<DslDefinition> rootDynamicDefinitions;

	/**
	 * Constructor.
	 * @param dynamicRegistryPlugins Grammar handlers
	 */
	CompositeDynamicRegistry(final List<DynamicRegistryPlugin> dynamicRegistryPlugins) {
		Assertion.checkNotNull(dynamicRegistryPlugins);
		//-----
		dynamicRegistries = new ArrayList<>(dynamicRegistryPlugins);
		//Création de la grammaire.
		grammar = createGrammar();

		rootDynamicDefinitions = new ArrayList<>();
		for (final DynamicRegistry dynamicRegistry : dynamicRegistries) {
			rootDynamicDefinitions.addAll(dynamicRegistry.getRootDynamicDefinitions());
		}
	}

	private DslGrammar createGrammar() {
		return () -> dynamicRegistries
				.stream()
				.flatMap(dynamicRegistry -> dynamicRegistry.getGrammar().getEntities().stream())
				.collect(Collectors.toList());
	}

	/** {@inheritDoc} */
	@Override
	public DslGrammar getGrammar() {
		return grammar;
	}

	/** {@inheritDoc} */
	@Override
	public List<DslDefinition> getRootDynamicDefinitions() {
		return rootDynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DslDefinition xdefinition, final DslDefinitionRepository dynamicModelrepository) {
		//Les entités du noyaux ne sont pas à gérer par des managers spécifiques.
		if (!xdefinition.getEntity().isProvided()) {
			final DynamicRegistry dynamicRegistry = lookUpDynamicRegistry(xdefinition);
			dynamicRegistry.onNewDefinition(xdefinition, dynamicModelrepository);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Definition createDefinition(final DefinitionSpace definitionSpace, final DslDefinition xdefinition) {
		try {
			// perf: ifs ordonnés en gros par fréquence sur les projets
			return lookUpDynamicRegistry(xdefinition)
					.createDefinition(definitionSpace, xdefinition);
		} catch (final Exception e) {
			//on catch tout (notament les assertions) car c'est ici qu'on indique l'URI de la définition posant problème
			throw new WrappedException("An error occurred during the creation of the following definition : " + xdefinition.getName(), e);
		}
	}

	private DynamicRegistry lookUpDynamicRegistry(final DslDefinition xdefinition) {
		//On regarde si la grammaire contient la métaDefinition.
		return dynamicRegistries
				.stream()
				.filter(dynamicRegistry -> dynamicRegistry.getGrammar().getEntities().contains(xdefinition.getEntity()))
				.findFirst()
				//Si on n'a pas trouvé de définition c'est qu'il manque la registry.
				.orElseThrow(() -> new IllegalArgumentException(xdefinition.getEntity().getName() + " " + xdefinition.getName() + " non traitée. Il manque une DynamicRegistry ad hoc."));
	}
}
