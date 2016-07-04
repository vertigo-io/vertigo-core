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
package io.vertigo.dynamo.collections;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Option;

/**
 * Processor that can be composed of filters or sorters
 * and be applied on a list.
 * @author pchretien
 */
public interface DtListProcessor {
	/**
	 * Add any function that transform a list into an another list.
	 * @param listFunction List function
	 * @return new DtListProcessor completed with this function
	 */
	DtListProcessor add(DtListFunction<?> listFunction);

	/**
	 * Création d'un tri de colonne.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @return Etat du tri
	 */
	DtListProcessor sort(final String fieldName, final boolean desc);

	//=======================FILTER============================================
	/**
	 * Constructeur d'un filtre champ = valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 * @return Filtre
	 */
	DtListProcessor filterByValue(final String fieldName, final Serializable value);

	/**
	 * Constructeur d'un filtre de range.
	 * @param fieldName Nom du champ
	 * @param min Valeur minimale
	 * @param max Valeur maximale
	 * @return Filtre
	 * @param <C> Type des bornes
	 */
	<C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 *
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 */
	DtListProcessor filter(final ListFilter listFilter);

	//=======================SUB LIST==========================================
	/**
	 * Sous Liste d'une DTC, ne modifie pas la collection d'origine.
	 * @param start Indexe de début (Inclus)
	 * @param end Indexe de fin (Exclus)
	 * @return Collection filtrée
	 */
	DtListProcessor filterSubList(final int start, final int end);

	//=========================================================================
	//=========================================================================
	/**
	 * Apply composed functions to list
	 * @param <D> List element's type
	 * @param input List ( will be unchanged)
	 * @return a new List
	 */
	<D extends DtObject> DtList<D> apply(final DtList<D> input);
}
