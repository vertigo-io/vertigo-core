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

import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Manager;

/**
 * Some tools on collections/lists to allow
 *  - sort
 *  - filter
 *  - facets.
 * @author  pchretien
 */
public interface CollectionsManager extends Manager {
	/**
	 * Filter or sort a list via a listProcessor, can be composed of filters or sorters.
	 * @return DtListProcessor
	 */
	DtListProcessor createDtListProcessor();

	/**
	 * Filter or sort a list via a listProcessor powered by an index engine, can be composed of filters or sorters.
	 * @return DtListIndexProcessor
	 * @param <D> Type de l'objet de la liste
	 */
	<D extends DtObject> IndexDtListFunctionBuilder<D> createIndexDtListFunctionBuilder();

	/**
	 * Facettage d'une liste selon une requete.
	 * Le facettage s'effectue en deux temps :
	 *  - Filtrage de la liste
	 *  - Facettage proprement dit
	 * @param dtList Liste à facetter
	 * @param facetedQuery Requete à appliquer (filtrage)
	 * @return Résultat correspondant à la requête
	 */
	<R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery);
}
