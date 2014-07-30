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
package io.vertigo.dynamo.impl.collections.facet.model;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.Facet;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Factory de FacetedQueryDefinition.
 * Permet de créer les définitions avant de les enregistrer dans via la registry dans le namespace.
 * @author pchretien, npiedeloup
 */
public final class FacetFactory {
	private final CollectionsManager collectionManager;

	public FacetFactory(final CollectionsManager collectionManager) {
		Assertion.checkNotNull(collectionManager);
		//---------------------------------------------------------------------
		this.collectionManager = collectionManager;
	}

	/**
	 * Création d'une liste de facettes à partir d'une liste.
	 * @param facetedQueryDefinition Requête
	 * @param dtList Liste 
	 * @return Liste des facettes.
	 */
	public List<Facet> createFacets(final FacetedQueryDefinition facetedQueryDefinition, final DtList<?> dtList) {
		Assertion.checkNotNull(facetedQueryDefinition);
		Assertion.checkNotNull(dtList);
		//---------------------------------------------------------------------
		final List<Facet> facets = new ArrayList<>();
		//Pour chaque type de facette
		for (final FacetDefinition facetDefinition : facetedQueryDefinition.getFacetDefinitions()) {
			facets.add(createFacet(facetDefinition, dtList));
		}
		return facets;
	}

	private <D extends DtObject> DtList<D> apply(final ListFilter listFilter, final DtList<D> fullDtList) {
		//on délégue à CollectionsManager les méthodes de requête de filtrage.
		return collectionManager.<D> createFilter(listFilter)//
				.apply(fullDtList);
	}

	private Facet createFacet(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		if (facetDefinition.isRangeFacet()) {
			//Cas des facettes par 'range' 
			return createFacetRange(facetDefinition, dtList);
		}
		//Cas des facettes par 'term'
		return createTermFacet(facetDefinition, dtList);
	}

	private Facet createFacetRange(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		//map résultat avec le count par FacetFilter
		final Map<FacetValue, Long> facetValues = new LinkedHashMap<>();

		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			//Pour chaque Valeur de facette on compte le nombre d'élements.
			final DtList<?> facetFilteredList = apply(facetRange.getListFilter(), dtList);
			facetValues.put(facetRange, Long.valueOf(facetFilteredList.size()));
		}
		return new Facet(facetDefinition, facetValues);
	}

	private static Facet createTermFacet(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		//Cas des facettes par Term 
		final DtField dtField = facetDefinition.getDtField();
		//on garde un index pour incrémenter le facetFilter pour chaque Term
		final Map<Object, FacetValue> facetFilterIndex = new HashMap<>();
		//et la map résultat avec le count par FacetFilter
		final Map<FacetValue, Long> facetValues = new HashMap<>();

		FacetValue facetValue;
		for (final DtObject dto : dtList) {
			final Object value = dtField.getDataAccessor().getValue(dto);
			facetValue = facetFilterIndex.get(value);
			if (facetValue == null) {
				final String valueAsString = dtField.getDomain().getFormatter().valueToString(value, dtField.getDomain().getDataType());
				final String stringLabel;
				if (StringUtil.isEmpty(valueAsString)) {
					stringLabel = "<==no label==>";
				} else {
					stringLabel = valueAsString;
				}
				final MessageText label = new MessageText(stringLabel, null);
				//on garde la syntaxe Solr pour l'instant
				final ListFilter listFilter = new ListFilter(dtField.getName() + ":\"" + valueAsString + "\"");
				facetValue = new FacetValue(listFilter, label);
				facetFilterIndex.put(value, facetValue);
				facetValues.put(facetValue, 0L);
			}
			facetValues.put(facetValue, facetValues.get(facetValue) + 1L);
		}

		//tri des facettes
		final Comparator<FacetValue> facetComparator = new Comparator<FacetValue>() {
			public int compare(final FacetValue o1, final FacetValue o2) {
				final int compareNbDoc = (int) (facetValues.get(o2) - facetValues.get(o1));
				return compareNbDoc != 0 ? compareNbDoc : o1.getLabel().getDisplay().compareToIgnoreCase(o2.getLabel().getDisplay());
			}
		};
		final Map<FacetValue, Long> sortedFacetValues = new TreeMap<>(facetComparator);
		sortedFacetValues.putAll(facetValues);
		return new Facet(facetDefinition, sortedFacetValues);
	}
}
