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
package io.vertigo.dynamo.collections.metamodel;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetDefinition.FacetOrder;
import io.vertigo.dynamo.collections.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.MessageText;

/**
 * Builder des Facettes par Range.
 *
 * @author pchretien
 */
public final class FacetDefinitionByRangeBuilder implements Builder<FacetDefinition> {
	private final String name;
	private final DtField dtField;
	private final MessageText label;
	private final List<FacetValue> facetRanges = new ArrayList<>();

	/**
	 * Constructor.
	 * @param name name
	 * @param dtField field
	 * @param label label
	 */
	public FacetDefinitionByRangeBuilder(final String name, final DtField dtField, final MessageText label) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dtField);
		Assertion.checkNotNull(label);
		//-----
		this.name = name;
		this.dtField = dtField;
		this.label = label;
	}

	/**
	 * Add facet value.
	 * @param query Query to use for filter
	 * @param facetValueLabel facet value label
	 * @return this builder
	 */
	public FacetDefinitionByRangeBuilder addFacetValue(final String code, final String query, final String facetValueLabel) {
		final ListFilter listFilter = new ListFilter(query);
		return addFacetValue(new FacetValue(code, listFilter, new MessageText(facetValueLabel, null)));
	}

	/**
	 * Add facet value.
	 * @param facetValue Facet value
	 * @return this builder
	 */
	public FacetDefinitionByRangeBuilder addFacetValue(final FacetValue facetValue) {
		Assertion.checkNotNull(facetValue);
		//-----
		facetRanges.add(facetValue);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public FacetDefinition build() {
		return FacetDefinition.createFacetDefinitionByRange(name, dtField, label, facetRanges, FacetOrder.definition);
	}
}
