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
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.metamodel.FacetDefinitionByRangeBuilder;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Option;
import io.vertigo.util.ClassUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pchretien
 */
public final class SearchDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {

	public SearchDynamicRegistryPlugin() {
		super(SearchGrammar.GRAMMAR);
	}

	/** {@inheritDoc} */
	@Override
	public Option<Definition> createDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdefinition) {
		final Definition definition;
		if (SearchGrammar.INDEX_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			definition = createIndexDefinition(definitionSpace, xdefinition);
		} else if (SearchGrammar.FACET_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			definition = createFacetDefinition(definitionSpace, xdefinition);
		} else if (SearchGrammar.FACETED_QUERY_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			definition = createFacetedQueryDefinition(xdefinition);
		} else {
			throw new IllegalStateException("unknown definition :" + xdefinition);
		}
		return Option.some(definition);
	}

	private static SearchIndexDefinition createIndexDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xsearchObjet) {
		final DtDefinition keyConceptDtDefinition = definitionSpace.resolve(xsearchObjet.getDefinitionName("keyConcept"), DtDefinition.class);
		final DtDefinition indexDtDefinition = definitionSpace.resolve(xsearchObjet.getDefinitionName("dtIndex"), DtDefinition.class);
		//	final List<FacetDefinition> facetDefinitions = Collections.emptyList();
		final String definitionName = xsearchObjet.getName();
		final String searchLoaderId = getPropertyValueAsString(xsearchObjet, SearchGrammar.SEARCH_LOADER_PROPERTY);
		final SearchIndexDefinition indexDefinition = new SearchIndexDefinition(definitionName, keyConceptDtDefinition, indexDtDefinition, searchLoaderId);
		//indexDefinition.makeUnmodifiable();
		return indexDefinition;
	}

	private static FacetDefinition createFacetDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition xdefinition) {
		//	final List<FacetDefinition> facetDefinitions = Collections.emptyList();
		final String definitionName = xdefinition.getName();
		final DtDefinition indexDtDefinition = definitionSpace.resolve(xdefinition.getDefinitionName("dtDefinition"), DtDefinition.class);
		final String dtFieldName = getPropertyValueAsString(xdefinition, SearchGrammar.FIELD_NAME);
		final DtField dtField = indexDtDefinition.getField(dtFieldName);
		final String label = getPropertyValueAsString(xdefinition, KspProperty.LABEL);

		//Déclaration des ranges
		final List<DynamicDefinition> rangeDefinitions = xdefinition.getChildDefinitions("range");
		final FacetDefinition facetDefinition;
		if (rangeDefinitions.isEmpty()) {
			facetDefinition = FacetDefinition.createFacetDefinitionByTerm(definitionName, dtField, new MessageText(label, null, (Serializable[]) null));
		} else {
			final FacetDefinitionByRangeBuilder facetDefinitionByRangeBuilder = new FacetDefinitionByRangeBuilder(definitionName, dtField, new MessageText(label, null, (Serializable[]) null));
			for (final DynamicDefinition rangeDefinition : rangeDefinitions) {
				final FacetValue facetValue = createFacetValue(rangeDefinition);
				facetDefinitionByRangeBuilder.addFacetValue(facetValue);
			}
			facetDefinition = facetDefinitionByRangeBuilder.build();
		}
		//indexDefinition.makeUnmodifiable();
		return facetDefinition;
	}

	private static FacetValue createFacetValue(final DynamicDefinition rangeDefinition) {
		final String listFilterString = getPropertyValueAsString(rangeDefinition, SearchGrammar.RANGE_FILTER_PROPERTY);
		final ListFilter listFilter = new ListFilter(listFilterString);
		final String labelString = getPropertyValueAsString(rangeDefinition, KspProperty.LABEL);
		final MessageText label = new MessageText(labelString, null, (Serializable[]) null);
		return new FacetValue(listFilter, label);
	}

	private static FacetedQueryDefinition createFacetedQueryDefinition(final DynamicDefinition xdefinition) {
		final String definitionName = xdefinition.getName();
		final DtDefinition keyConceptDtDefinition = Home.getApp().getDefinitionSpace().resolve(xdefinition.getDefinitionName("keyConcept"), DtDefinition.class);
		final List<String> dynamicFacetDefinitionNames = xdefinition.getDefinitionNames("facets");
		final List<FacetDefinition> facetDefinitions = new ArrayList<>();
		for (final String dynamicDefinitionName : dynamicFacetDefinitionNames) {
			final FacetDefinition facetDefinition = Home.getApp().getDefinitionSpace().resolve(dynamicDefinitionName, FacetDefinition.class);
			facetDefinitions.add(facetDefinition);
		}
		final String listFilterBuilderQuery = getPropertyValueAsString(xdefinition, SearchGrammar.LIST_FILTER_BUILDER_QUERY);
		final Class<? extends ListFilterBuilder> listFilterBuilderClass = getListFilterBuilderClass(xdefinition);
		final String criteriaDomainName = xdefinition.getDefinitionName("domainCriteria");
		final Domain criteriaDomain = Home.getApp().getDefinitionSpace().resolve(criteriaDomainName, Domain.class);

		final FacetedQueryDefinition facetedQueryDefinition = new FacetedQueryDefinition(definitionName, keyConceptDtDefinition, facetDefinitions, criteriaDomain, listFilterBuilderClass, listFilterBuilderQuery);
		//indexDefinition.makeUnmodifiable();
		return facetedQueryDefinition;
	}

	private static Class<? extends ListFilterBuilder> getListFilterBuilderClass(final DynamicDefinition xtaskDefinition) {
		final String listFilterBuilderClassName = getPropertyValueAsString(xtaskDefinition, SearchGrammar.LIST_FILTER_BUILDER_CLASS);
		return ClassUtil.classForName(listFilterBuilderClassName, ListFilterBuilder.class);
	}

}
