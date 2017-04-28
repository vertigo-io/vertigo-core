/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.collections;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.UnaryOperator;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Processor that can be composed of filters or sorters
 * and be applied on a list.
 * @author pchretien
 * @param <D> Type of list's element
 */
public interface DtListProcessor<D extends DtObject> {
	/**
	 * Add any function that transform a list into an another list.
	 * @param listFunction List function
	 * @return new DtListProcessor completed with this function
	 */
	DtListProcessor<D> add(UnaryOperator<DtList<D>> listFunction);

	/**
	 * Création d'un tri de colonne.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @return Etat du tri
	 */
	DtListProcessor<D> sort(final String fieldName, final boolean desc);

	//=======================FILTER============================================
	/**
	 * Constructeur d'un filtre champ = valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 * @return Filtre
	 */
	DtListProcessor<D> filterByValue(final String fieldName, final Serializable value);

	/**
	 * Constructeur d'un filtre de range.
	 * @param fieldName Nom du champ
	 * @param min Valeur minimale
	 * @param max Valeur maximale
	 * @return Filtre
	 * @param <C> Type des bornes
	 */
	<C extends Comparable<?>> DtListProcessor<D> filterByRange(final String fieldName, final Optional<C> min, final Optional<C> max);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 *
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 */
	DtListProcessor<D> filter(final ListFilter listFilter);

	//=========================================================================
	//=========================================================================
	/**
	 * Apply composed functions to list
	 * @param input List ( will be unchanged)
	 * @return a new List
	 */
	DtList<D> apply(final DtList<D> input);
}
