package io.vertigo.dynamox.search.dsl.definition;

public final class DslFixedQueryDefinition implements DslQueryDefinition {
	//(fixedQuery)
	private final String fixedQuery;

	@Override
	public String toString() {
		return fixedQuery;
	}

	public DslFixedQueryDefinition(final String fixedQuery) {
		this.fixedQuery = fixedQuery;
	}

	/**
	 * @return fixedQuery
	 */
	public final String getFixedQuery() {
		return fixedQuery;
	}

}
