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
package io.vertigo.dynamo.collections.facet.model;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.util.List;

/**
 * Requete de filtrage par facettes.
 * @author npiedeloup
 */
public final class FacetedQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final DefinitionReference<FacetedQueryDefinition> facetedQueryDefinition;
	private final List<ListFilter> listFilters;

	/**
	 * Constructeur.
	 * @param facetedQueryDefinition Definition de la requête
	 * @param listFilters Liste de filtres supplémentaires
	 */
	public FacetedQuery(final FacetedQueryDefinition facetedQueryDefinition, final List<ListFilter> listFilters) {
		Assertion.checkNotNull(facetedQueryDefinition);
		Assertion.checkNotNull(listFilters);
		//---------------------------------------------------------------------
		this.facetedQueryDefinition = new DefinitionReference<>(facetedQueryDefinition);
		this.listFilters = listFilters;
	}

	/**
	 * @return Définition du FacetedQuery.
	 */
	public FacetedQueryDefinition getDefinition() {
		return facetedQueryDefinition.get();
	}

	/**
	 * Liste de filtres supplémentaires.
	 * @return Liste des filtres.
	 */
	public List<ListFilter> getListFilters() {
		return listFilters;
	}
}
