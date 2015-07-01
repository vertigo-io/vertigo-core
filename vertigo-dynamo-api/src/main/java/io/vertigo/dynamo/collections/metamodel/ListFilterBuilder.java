package io.vertigo.dynamo.collections.metamodel;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.lang.Builder;

/**
 * Project specific builder from Criteria to ListFilter.
 * @author npiedeloup
 * @param <C> Criteria type
 */
public interface ListFilterBuilder<C> extends Builder<ListFilter> {

	/**
	 * Build Query.
	 * @param buildQuery Query use by builder
	 * @return this builder
	 */
	ListFilterBuilder<C> withBuildQuery(String buildQuery);

	/**
	 * Process a criteria to produce a ListFilter.
	 * @param criteria Criteria
	 * @return this builder
	 */
	ListFilterBuilder<C> withCriteria(C criteria);

	/** {@inheritDoc} */
	@Override
	ListFilter build();

}
