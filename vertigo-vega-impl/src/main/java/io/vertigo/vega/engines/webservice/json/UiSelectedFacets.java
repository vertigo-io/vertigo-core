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
package io.vertigo.vega.engines.webservice.json;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.model.FacetValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Selected facets.
 * @author npiedeloup
 */
public final class UiSelectedFacets extends HashMap<String, String> {

	private static final long serialVersionUID = -6356451500854322017L;

	/**
	 * Convert this Selected Facets to a list of ListFilter.
	 * @return ListFilter for these Facets
	 */
	public List<ListFilter> toListFilters() {
		final List<ListFilter> listFilters = new ArrayList<>(size());
		// facet selection list.
		for (final Map.Entry<String, String> entry : entrySet()) {
			final FacetDefinition facetDefinition = Home.getApp().getDefinitionSpace().resolve(entry.getKey(),
					FacetDefinition.class);
			if (facetDefinition.isRangeFacet()) {
				for (final FacetValue facet : facetDefinition.getFacetRanges()) {
					if (facet.getLabel().getDisplay().equals(entry.getValue())) {
						listFilters.add(facet.getListFilter());
						break;
					}
				}
			} else {
				final ListFilter filter = new ListFilter(
						facetDefinition.getDtField().getName() + ":\"" + entry.getValue() + "\"");
				listFilters.add(filter);
			}
		}
		return listFilters;
	}

}
