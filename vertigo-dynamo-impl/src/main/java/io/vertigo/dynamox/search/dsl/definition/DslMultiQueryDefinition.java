package io.vertigo.dynamox.search.dsl.definition;

import java.util.List;

public final class DslMultiQueryDefinition implements DslQueryDefinition {
	//(preMultiQuery)\((queries|fixedQuery|multiQuery)+\)(postMultiQuery)
	private final String preMultiQuery;
	private final List<DslQueryDefinition> queries;
	private final String postMultiQuery;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preMultiQuery).append("(");
		for (final DslQueryDefinition query : queries) {
			sb.append(query);
		}
		sb.append(")").append(postMultiQuery);
		return sb.toString();
	}

	public DslMultiQueryDefinition(final String preMultiQuery, final List<DslQueryDefinition> queries, final String postMultiQuery) {
		this.preMultiQuery = preMultiQuery;
		this.queries = queries;
		this.postMultiQuery = postMultiQuery;
	}

	/**
	 * @return preMultiQuery
	 */
	public final String getPreMultiQuery() {
		return preMultiQuery;
	}

	/**
	 * @return queries
	 */
	public final List<DslQueryDefinition> getQueries() {
		return queries;
	}

	/**
	 * @return postMultiQuery
	 */
	public final String getPostMultiQuery() {
		return postMultiQuery;
	}
}
