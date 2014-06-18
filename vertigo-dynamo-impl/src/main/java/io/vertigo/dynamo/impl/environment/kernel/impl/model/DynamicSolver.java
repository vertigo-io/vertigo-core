package io.vertigo.dynamo.impl.environment.kernel.impl.model;

import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.kernel.exception.VRuntimeException;

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
			throw new VRuntimeException(" Les clés suivantes " + orphanCollection + " sont orphelines");
		}
		//----------------------------------------------------------------------
		final Collection<DynamicDefinition> coll = new ArrayList<>(definitionModelRepository.getDefinitions());

		DynamicDefinition xdef = null;
		int size = coll.size();
		while (size > 0) {
			for (final Iterator<DynamicDefinition> it = coll.iterator(); it.hasNext();) {
				xdef = it.next();
				//==============================================================
				//==============================================================
				//On vérifie que les sous éléments sont résolues
				if (isSolved(definitionModelRepository, orderedList, xdef)) {
					orderedList.add(xdef);
					it.remove();
				}
			}
			//Si la liste n'a pas diminuée c'est que l'on a fini de résoudre ce qui peut l'être.
			if (size == coll.size()) {
				throw new VRuntimeException(" Les références " + coll + " ne peuvent être résolues");
			}
			size = coll.size();
		}
		return orderedList;
	}

	private boolean isSolved(final DynamicDefinitionRepository definitionModelRepository, final List<DynamicDefinition> orderedList, final DynamicDefinition xdef) {
		//Une définition est résolue ssi toutes ses sous definitions sont résolues

		//On vérifie que toutes les références sont connues
		for (final DynamicDefinitionKey dynamicDefinitionKey : xdef.getAllDefinitionKeys()) {
			final DynamicDefinition subDefinition = definitionModelRepository.getDefinition(dynamicDefinitionKey);
			if (!orderedList.contains(subDefinition)) {
				return false;
			}
		}

		//On vérifie que les composites sont résolues.
		for (final DynamicDefinition dynamicDefinition : xdef.getAllChildDefinitions()) {
			if (!isSolved(definitionModelRepository, orderedList, dynamicDefinition)) {
				return false;
			}
		}
		return true;
	}
}
