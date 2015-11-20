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
package io.vertigo.core.definition.dsl.dynamic;

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Espace de nommage.
 * Les éléments de la grammaire à savoir les définitions sont ajoutées à la volée.
 * Les définitions doivent respecter une métagrammaire définie préalablement dans DynamicNameSpace.
 *
 * @author  pchretien
 */
public final class DynamicDefinitionRepository {

	/***
	 * On retient les définitions dans l'ordre pour
	 * créer les fichiers toujours de la même façon.
	 */
	private final Map<String, DynamicDefinition> definitions = new LinkedHashMap<>();
	private final List<DynamicDefinition> templates = new ArrayList<>();

	private final DynamicRegistry registry;
	private final EntityGrammar grammar;

	/**
	 * Constructeur.
	 * @param registry DynamicDefinitionHandler
	 */
	public DynamicDefinitionRepository(final DynamicRegistry registry) {
		Assertion.checkNotNull(registry);
		//-----
		this.registry = registry;
		grammar = registry.getGrammar();
	}

	/**
	 * @return Grammaire
	 */
	public EntityGrammar getGrammar() {
		return grammar;
	}

	/**
	 * Teste si une définition a déjà été ajoutée.
	 * @param definitionKey Clé de la définition
	 * @return Si la définition a déjà été enregistrée
	 */
	public boolean containsDefinitionName(final String definitionName) {
		return definitions.containsKey(definitionName);
	}

	/**
	 * Récupération d'une définition par sa clé
	 *  -Soit la clé n'existe pas
	 *  -Soit la clé existe mais sans aucune définition
	 *  -Soit la clé raméne une définition.
	 *
	 * @param definitionKey Clé de la définition
	 * @return DynamicDefinition Définition correspondante ou null.
	 */
	public DynamicDefinition getDefinition(final String definitionName) {
		Assertion.checkArgument(definitions.containsKey(definitionName), "Aucune clé enregistrée pour :{0} parmi {1}", definitionName, definitions.keySet());
		//-----
		final DynamicDefinition definition = definitions.get(definitionName);
		//-----
		Assertion.checkNotNull(definition, "Clé trouvée mais pas de définition enregistrée trouvée pour {0}", definitionName);
		return definition;
	}

	/**
	 * Résolution des références de définitions.
	 */
	public void solve(final DefinitionSpace definitionSpace) {
		Assertion.checkNotNull(definitionSpace);
		//-----
		final DynamicSolver solver = new DynamicSolver();

		solveTemplates();
		final List<DynamicDefinition> sortedDynamicDefinitions = solver.solve(definitionSpace, this);
		registerAllDefinitions(definitionSpace, sortedDynamicDefinitions);
	}

	private void solveTemplates() {
		for (final DynamicDefinition template : templates) {
			((DynamicDefinitionBuilder) getDefinition(template.getName())).addBody(template);
		}
	}

	private void registerAllDefinitions(final DefinitionSpace definitionSpace, final List<DynamicDefinition> sortedDynamicDefinitions) {
		for (final DynamicDefinition xdefinition : sortedDynamicDefinitions) {
			DynamicValidator.check(xdefinition);
			final Option<Definition> definitionOption = registry.createDefinition(definitionSpace, xdefinition);
			if (definitionOption.isDefined()) {
				definitionSpace.put(definitionOption.get());
			}
		}
	}

	/**
	 * Ajoute une définition.
	 * @param definition DynamicDefinition
	 */
	public void addDefinition(final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		//On enregistre la définition qu'elle soit renseignée ou null.
		final DynamicDefinition previousDefinition = definitions.put(definition.getName(), definition);
		//On vérifie que l'on n'essaie pas d'écraser la définition déjà présente.
		Assertion.checkState(previousDefinition == null, "la définition {0} est déjà enregistrée", definition.getName());
		//-----
		registry.onNewDefinition(definition, this);
	}

	/**
	 * Ajoute un template.
	 * @param definition Template de définition
	 */
	public void addTemplate(final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		templates.add(definition);
	}

	/**
	 * Création d'une Definition (Non enregistrée !).
	 * @param packageName Nom du package
	 * @param keyName Nom de la Définition
	 * @param entity Entité
	 * @return Nouvelle Définition
	 */
	public static DynamicDefinitionBuilder createDynamicDefinitionBuilder(final String definitionName, final Entity entity, final String packageName) {
		return new DynamicDefinitionImpl(definitionName, entity).withPackageName(packageName);
	}

	/**
	 *  @return Liste des clés orphelines.
	 */
	Collection<String> getOrphanDefinitionKeys() {
		final Collection<String> collection = new ArrayList<>();
		for (final Entry<String, DynamicDefinition> entry : definitions.entrySet()) {
			if (entry.getValue() == null) {
				collection.add(entry.getKey());
			}
		}
		return Collections.unmodifiableCollection(collection);
	}

	/**
	 * @return Liste des définitions complétes
	 */
	Collection<DynamicDefinition> getDefinitions() {
		return Collections.unmodifiableCollection(definitions.values());
	}
}
