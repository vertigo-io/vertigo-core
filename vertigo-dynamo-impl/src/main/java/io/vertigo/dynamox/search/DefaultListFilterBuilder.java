package io.vertigo.dynamox.search;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.lang.Assertion;

/**
 * Default builder from Criteria to ListFilter with a query pattern with DSL.
 * Pattern syntax is easy :
 * #CRITERIA# : criteria.toString() : use this when Criteria is a user string
 * #MY_FIELD# : criteria.myField
 *
 * example:
 *  +#USER_QUERY# //directly use user's query
 *  +CODE:#CODE#  //CODE equals strictly
 *  +COMMENT:#COMMENT#*  //COMMENT contains words prefixed with criteria's comment words (all words)
 *  +YEAR:[#YEAR_MIN#, #YEAR_MAX#] //YEAR between crieteria's year_min and year_max
 *  +(ADDR1:#ADDRESS#* ADDR2:#ADDRESS#*) //criteria ADDRESS field should be in ADDR1 or ADDR2 index's fields
 *
 * If a criteria field contains OR / AND it will be use as logical operator.
 * If a criteria field contains XXX:yyyy it will be use as a specific field query and will not be transformed
 *
 * @author npiedeloup
 * @param <C> Criteria type
 */
public final class DefaultListFilterBuilder<C> implements ListFilterBuilder<C> {

	public static final String DEFAULT_QUERY = "#CRITERIA#";
	private String buildQuery;
	private C criteria;

	/**
	 * Fix query pattern.
	 * @param newBuildQuery Pattern (not null, could be empty)
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withBuildQuery(final String newBuildQuery) {
		Assertion.checkNotNull(newBuildQuery);
		Assertion.checkState(buildQuery == null, "query was already set : {0}", buildQuery);
		//-----
		this.buildQuery = newBuildQuery;
		return this;
	}

	/**
	 * Fix criteria.
	 * @param newCriteria Criteria
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withCriteria(final C newCriteria) {
		Assertion.checkNotNull(newCriteria);
		Assertion.checkState(criteria == null, "criteria was already set : {0}", criteria);
		//-----
		this.criteria = newCriteria;
		return this;

	}

	/** {@inheritDoc} */
	@Override
	public ListFilter build() {
		return new ListFilter(criteria.toString()); //TODO
	}

}
