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
package io.vertigo.dynamo.plugins.environment.registries.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetDefinitionByRangeBuilder;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.lang.MessageText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pchretien
 */
public final class SearchDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {

	public SearchDynamicRegistryPlugin() {
		super(SearchGrammar.GRAMMAR);
		Home.getDefinitionSpace().register(SearchIndexDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public void onDefinition(final DynamicDefinition xdefinition) {
		if (SearchGrammar.INDEX_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			final SearchIndexDefinition indexDefinition = createIndexDefinition(xdefinition);
			Home.getDefinitionSpace().put(indexDefinition, SearchIndexDefinition.class);
		} else if (SearchGrammar.FACET_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			final FacetDefinition facetDefinition = createFacetDefinition(xdefinition);
			Home.getDefinitionSpace().put(facetDefinition, FacetDefinition.class);
		} else if (SearchGrammar.FACETED_QUERY_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			final FacetedQueryDefinition facetedQueryDefinition = createFacetedQueryDefinition(xdefinition);
			Home.getDefinitionSpace().put(facetedQueryDefinition, FacetedQueryDefinition.class);
		}
	}

	private static SearchIndexDefinition createIndexDefinition(final DynamicDefinition xsearchObjet) {
		final DtDefinition subjectDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtSubject").getName(), DtDefinition.class);
		final DtDefinition indexDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtIndex").getName(), DtDefinition.class);
		//	final List<FacetDefinition> facetDefinitions = Collections.emptyList();
		final String definitionName = xsearchObjet.getDefinitionKey().getName();
		final String searchLoaderId = (String) xsearchObjet.getPropertyValue(SearchGrammar.SEARCH_LOADER_PROPERTY);
		final SearchIndexDefinition indexDefinition = new SearchIndexDefinition(definitionName, subjectDtDefinition, indexDtDefinition, searchLoaderId);
		//indexDefinition.makeUnmodifiable();
		return indexDefinition;
	}

	private FacetDefinition createFacetDefinition(final DynamicDefinition xdefinition) {
		//	final List<FacetDefinition> facetDefinitions = Collections.emptyList();
		final String definitionName = xdefinition.getDefinitionKey().getName();
		final DtDefinition indexDtDefinition = Home.getDefinitionSpace().resolve(xdefinition.getDefinitionKey("dtDefinition").getName(), DtDefinition.class);
		final String dtFieldName = (String) xdefinition.getPropertyValue(SearchGrammar.FIELD_NAME);
		final DtField dtField = indexDtDefinition.getField(dtFieldName);
		final String label = (String) xdefinition.getPropertyValue(KspProperty.LABEL);

		//Déclaration des ranges
		final List<DynamicDefinition> rangeDefinitions = xdefinition.getChildDefinitions("range");
		final FacetDefinition facetDefinition;
		if (rangeDefinitions.isEmpty()) {
			facetDefinition = FacetDefinition.createFacetDefinitionByTerm(definitionName, dtField, new MessageText(label, null, (Serializable[]) null));
		} else {
			final FacetDefinitionByRangeBuilder facetDefinitionByRangeBuilder = new FacetDefinitionByRangeBuilder(definitionName, dtField, new MessageText(label, null, (Serializable[]) null));
			for (final DynamicDefinition rangeDefinition : rangeDefinitions) {
				final FacetValue facetValue = createFacetValue(rangeDefinition);
				facetDefinitionByRangeBuilder.withFacetValue(facetValue);
			}
			facetDefinition = facetDefinitionByRangeBuilder.build();
		}
		//indexDefinition.makeUnmodifiable();
		return facetDefinition;
	}

	private FacetValue createFacetValue(final DynamicDefinition rangeDefinition) {
		final String listFilterString = (String) rangeDefinition.getPropertyValue(SearchGrammar.RANGE_FILTER_PROPERTY);
		final ListFilter listFilter = new ListFilter(listFilterString);
		final String labelString = (String) rangeDefinition.getPropertyValue(KspProperty.LABEL);
		final MessageText label = new MessageText(labelString, null, (Serializable[]) null);
		return new FacetValue(listFilter, label);
	}

	private FacetedQueryDefinition createFacetedQueryDefinition(final DynamicDefinition xdefinition) {
		final String definitionName = xdefinition.getDefinitionKey().getName();
		final List<DynamicDefinitionKey> dynamicFacetDefinitionKeys = xdefinition.getDefinitionKeys("facet");
		final List<FacetDefinition> facetDefinitions = new ArrayList<>();
		for (final DynamicDefinitionKey dynamicDefinitionKey : dynamicFacetDefinitionKeys) {
			final FacetDefinition facetDefinition = Home.getDefinitionSpace().resolve(dynamicDefinitionKey.getName(), FacetDefinition.class);
			facetDefinitions.add(facetDefinition);
		}
		final FacetedQueryDefinition facetedQueryDefinition = new FacetedQueryDefinition(definitionName, facetDefinitions);
		//indexDefinition.makeUnmodifiable();
		return facetedQueryDefinition;
	}
}
