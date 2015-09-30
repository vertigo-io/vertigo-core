package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * Fixed content.
 * (fixedQuery)
 * @author npiedeloup
 */
public final class DslFixedQueryDefinition implements DslQueryDefinition {
	private final String fixedQuery;

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return fixedQuery;
	}

	/**
	 * @param fixedQuery Fixed content
	 */
	public DslFixedQueryDefinition(final String fixedQuery) {
		Assertion.checkNotNull(fixedQuery);
		//-----
		this.fixedQuery = fixedQuery;
	}

	/**
	 * @return fixedQuery
	 */
	public final String getFixedQuery() {
		return fixedQuery;
	}

}
