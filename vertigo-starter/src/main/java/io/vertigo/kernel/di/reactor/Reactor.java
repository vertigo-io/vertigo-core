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
package io.vertigo.kernel.di.reactor;

import io.vertigo.kernel.di.DIException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * R�acteur.
 * Objet dans lequel on met en vrac des composants.
 * Puis il suffit d'appeler la m�thode  'proceed'  
 *   
 * @author pchretien
 */
public final class Reactor {
	//Map des composants et de leurs parents
	private final Set<String> allComponentInfos = new HashSet<>();
	private final List<DIComponentInfo> diComponentInfos = new ArrayList<>();
	private final Set<String> parentComponentInfos = new HashSet<>();

	private void check(final String id) {
		//On v�rifie que l'on n'ins�re pas deux composants avec le m�me id.
		if (allComponentInfos.contains(id)) {
			throw new DIException("deux composants ins�r�s avec le m�me id :'" + id + "'");
		}
	}

	public Reactor addComponent(final String id, final Class<?> implClass) {
		return addComponent(id, implClass, Collections.<String> emptySet(), Collections.<String> emptySet());
	}

	public Reactor addComponent(final String id, final Class<?> implClass, final Set<String> pluginIds, final Set<String> params) {
		final DIComponentInfo diComponentInfo = new DIComponentInfo(id, implClass, pluginIds, params);
		check(diComponentInfo.getId());
		allComponentInfos.add(diComponentInfo.getId());
		diComponentInfos.add(diComponentInfo);
		return this;
	}

	/**
	 * Ajout d'un comoposant identifi� par son seul id.
	 */
	public Reactor addParent(final String id) {
		check(id);
		allComponentInfos.add(id);
		parentComponentInfos.add(id);
		return this;
	}

	/**
	 * M�thode cl� permettant d'activer la r�action.
	 * @return Liste ordonn�e des ids de composants, selon leur d�pendances. 
	 */
	public List<String> proceed() {
		//-----------------------------------------------------------------------------------------
		//1.On v�rifie si tous les composants d�finis par leurs ids existent
		final List<DIDependency> missing = new ArrayList<>();
		for (final DIComponentInfo componentInfo : diComponentInfos) {
			for (final DIDependency dependency : componentInfo.getDependencies()) {
				//Si une r�f�rence est optionnelle alors elle n'est jamais manquante.
				if (!dependency.isOptional() && !allComponentInfos.contains(dependency.getId())) {
					missing.add(dependency);
				}
			}
		}
		if (!missing.isEmpty()) {
			throw new DIException("Components not found :" + missing + "\n\tLoaded components : " + diComponentInfos);
		}
		//-----------------------------------------------------------------------------------------
		//2.On r�sout les d�pendances
		final List<DIComponentInfo> unsorted = new ArrayList<>(diComponentInfos);
		//Niveaux de d�pendances des composants 
		//		final List<List<String>> levels = new ArrayList<>();
		final List<String> sorted = new ArrayList<>();

		//. Par d�faut on consid�re comme tri�s tous les parents
		while (!unsorted.isEmpty()) {
			final int countSorted = sorted.size();
			for (final Iterator<DIComponentInfo> iterator = unsorted.iterator(); iterator.hasNext();) {
				final DIComponentInfo componentInfo = iterator.next();
				boolean solved = true;
				for (final DIDependency dependency : componentInfo.getDependencies()) {
					//On v�rifie si pour un composant 
					//TOUTES ses d�pendances sont bien d�j� r�solues.
					if (allComponentInfos.contains(dependency.getId()) || !dependency.isOptional()) {
						//On doit r�soudre toutes des r�f�rences connues(y compris les r�f�renes optionnelles) sans tenir compte des r�f�rences inconnues et optionnelles.
						solved = solved && (sorted.contains(dependency.getId()) || parentComponentInfos.contains(dependency.getId()));
					}
					if (!solved) {
						//Si ce n'est pas le cas on passe au composant suivant.
						//On arr�te de regarder les autres d�pendances
						break;
					}
				}
				if (solved) {
					//Le composant est r�solu
					// - On l'ajoute sa cl� � la liste des cl�s de composants r�solus 
					// - On le supprime de la liste des composants � r�soudre
					sorted.add(componentInfo.getId());
					iterator.remove();
				}
			}
			// Si lors d'une it�ration on ne fait rien c'est qu'il y a une d�pendance cyclique
			if (countSorted == sorted.size()) {
				// On a une d�pendance cyclique !
				throw new DIException("Liste des composants non r�solus :" + unsorted);
			}
		}
		//-----------------------------------------------------------------------------------------
		//3 On expose un liste de ids et non les composantInfos
		return Collections.unmodifiableList(sorted);

	}
}
