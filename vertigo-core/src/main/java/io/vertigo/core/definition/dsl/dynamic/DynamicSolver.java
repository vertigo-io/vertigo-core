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
package io.vertigo.core.definition.dsl.dynamic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

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
	List<DynamicDefinition> solve(final DefinitionSpace definitionSpace, final DynamicDefinitionRepository definitionRepository) {
		Assertion.checkNotNull(definitionSpace);
		Assertion.checkNotNull(definitionRepository);
		//-----
		//Liste des clés résolues
		final List<DynamicDefinition> orderedList = new ArrayList<>();

		final Collection<String> orphans = definitionRepository.getOrphanDefinitionKeys();
		if (!orphans.isEmpty()) {
			throw new VSystemException(" Les clés suivantes {0} sont orphelines", orphans);
		}
		//-----
		final Collection<DynamicDefinition> coll = new ArrayList<>(definitionRepository.getDefinitions());

		DynamicDefinition xdef;
		int size = coll.size();
		while (size > 0) {
			for (final Iterator<DynamicDefinition> it = coll.iterator(); it.hasNext();) {
				xdef = it.next();
				//==============================================================
				//==============================================================
				//On vérifie que les sous éléments sont résolues
				if (isSolved(definitionSpace, definitionRepository, orderedList, xdef, xdef)) {
					orderedList.add(xdef);
					it.remove();
				}
			}
			//Si la liste n'a pas diminuée c'est que l'on a fini de résoudre ce qui peut l'être.
			if (size == coll.size()) {
				throw new VSystemException(" Les références {0} ne peuvent être résolues", coll);
			}
			size = coll.size();
		}
		return orderedList;
	}

	private static boolean isSolved(final DefinitionSpace definitionSpace,
			final DynamicDefinitionRepository definitionRepository,
			final List<DynamicDefinition> orderedList,
			final DynamicDefinition definition,
			final DynamicDefinition xdefRoot) {
		//A definition is solved if all its sub definitions have been solved

		//We check all references were known
		for (final String fieldName : definition.getAllDefinitionLinkFieldNames()) {
			for (final String definitionName : definition.getDefinitionLinkNames(fieldName)) {
				//reference should be already solved in a previous resources module : then continue
				if (!definitionSpace.containsDefinitionName(definitionName)) {
					//or references should be in currently parsed resources
					if (!definitionRepository.containsDefinitionName(definitionName)) {
						final String xdefRootName = xdefRoot.getName().equals(definition.getName()) ? xdefRoot.getName() : xdefRoot.getName() + "." + definition.getName();
						throw new VSystemException("Clé {0} de type {3}, référencée par la propriété {2} de {1} non trouvée", definitionName, xdefRootName, fieldName, definition.getEntity().getField(fieldName).getType());
					}
					final DynamicDefinition linkedDefinition = definitionRepository.getDefinition(definitionName);
					if (!orderedList.contains(linkedDefinition)) {
						return false;
					}
				}
			}
		}

		//On vérifie que les composites sont résolues.
		for (final DynamicDefinition child : definition.getAllChildDefinitions()) {
			if (!isSolved(definitionSpace, definitionRepository, orderedList, child, xdefRoot)) {
				return false;
			}
		}
		return true;
	}
}
