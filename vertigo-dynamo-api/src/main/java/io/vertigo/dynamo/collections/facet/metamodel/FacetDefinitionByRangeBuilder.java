package io.vertigo.dynamo.collections.facet.metamodel;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder des Facettes par Range.
 *
 * @author pchretien 
 */
public final class FacetDefinitionByRangeBuilder implements Builder<FacetDefinition> {
	final String name;
	final DtField dtField;
	final MessageText label;
	final List<FacetValue> facetRanges = new ArrayList<>();

	public FacetDefinitionByRangeBuilder(final String name, final DtField dtField, final MessageText label) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(dtField);
		Assertion.checkNotNull(label);
		//-----------------------------------------------------------------
		this.name = name;
		this.dtField = dtField;
		this.label = label;
	}

	public FacetDefinitionByRangeBuilder withFacetValue(final String query, final String facetValueLabel) {
		final ListFilter listFilter = new ListFilter(query);
		return withFacetValue(new FacetValue(listFilter, new MessageText(facetValueLabel, null)));
	}

	public FacetDefinitionByRangeBuilder withFacetValue(final FacetValue facetValue) {
		Assertion.checkNotNull(facetValue);
		//-----------------------------------------------------------------
		facetRanges.add(facetValue);
		return this;
	}

	/** {@inheritDoc} */
	public FacetDefinition build() {
		return FacetDefinition.createFacetDefinitionByRange(name, dtField, label, facetRanges);
	}
}
