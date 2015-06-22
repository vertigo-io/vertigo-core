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
package io.vertigo.dynamo.impl.environment.kernel.impl.model;

import io.vertigo.core.Home;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Solver permet de résoudre les références.
 * Les références peuvent être orphelines : la clé ne correspond à aucune définition.
 * Les références circulaires ne peuvent pas être résolues.
 * Le solver est une fonction stateless qui prend en entrée le Repository du Model et calcule en sortie la liste des définitions.
 *
 * @author  pchretien
 */
final class DynamicSolver {
	/**
	* Résoltuion des références.
	* On appelle SyntaxHandler dans le bon Ordre
	*/
	List<DynamicDefinition> solve(final DynamicDefinitionRepository definitionModelRepository) {
		//Liste des clés résolues
		final List<DynamicDefinition> orderedList = new ArrayList<>();

		final Collection<DynamicDefinitionKey> orphanCollection = definitionModelRepository.getOrphanDefinitionKeys();
		if (!orphanCollection.isEmpty()) {
			throw new RuntimeException(" Les clés suivantes " + orphanCollection + " sont orphelines");
		}
		//-----
		final Collection<DynamicDefinition> coll = new ArrayList<>(definitionModelRepository.getDefinitions());

		DynamicDefinition xdef = null;
		int size = coll.size();
		while (size > 0) {
			for (final Iterator<DynamicDefinition> it = coll.iterator(); it.hasNext();) {
				xdef = it.next();
				//==============================================================
				//==============================================================
				//On vérifie que les sous éléments sont résolues
				if (isSolved(definitionModelRepository, orderedList, xdef, xdef)) {
					orderedList.add(xdef);
					it.remove();
				}
			}
			//Si la liste n'a pas diminuée c'est que l'on a fini de résoudre ce qui peut l'être.
			if (size == coll.size()) {
				throw new RuntimeException(" Les références " + coll + " ne peuvent être résolues");
			}
			size = coll.size();
		}
		return orderedList;
	}

	private boolean isSolved(final DynamicDefinitionRepository definitionModelRepository, final List<DynamicDefinition> orderedList, final DynamicDefinition xdef, final DynamicDefinition xdefRoot) {
		//A definition is solved if all its sub definitions have been solved

		//We check all references were known
		for (final DynamicDefinitionKey dynamicDefinitionKey : xdef.getAllDefinitionKeys()) {
			//reference should be already solved in a previous resources module : then continue
			if (!Home.getDefinitionSpace().containsDefinitionName(dynamicDefinitionKey.getName())) {
				//or references should be in currently parsed resources
				if (!definitionModelRepository.containsDefinitionKey(dynamicDefinitionKey)) {
					throw new RuntimeException("Clé " + dynamicDefinitionKey.getName() + " référencée par " + xdefRoot.getDefinitionKey().getName() + " non trouvée");
				}
				final DynamicDefinition subDefinition = definitionModelRepository.getDefinition(dynamicDefinitionKey);
				if (!orderedList.contains(subDefinition)) {
					return false;
				}
			}
		}

		//On vérifie que les composites sont résolues.
		for (final DynamicDefinition dynamicDefinition : xdef.getAllChildDefinitions()) {
			if (!isSolved(definitionModelRepository, orderedList, dynamicDefinition, xdefRoot)) {
				return false;
			}
		}
		return true;
	}
}
