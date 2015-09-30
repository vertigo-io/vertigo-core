package io.vertigo.dynamox.search.dsl.definition;


public final class DslRangeQueryDefinition implements DslQueryDefinition {
	//(preRangeQuery)\[(termQuery|fixedQuery) to (termQuery|fixedQuery)\](postRangeQuery)
	private final String preRangeQuery;
	private final DslQueryDefinition startQueryDefinitions;
	private final DslQueryDefinition endQueryDefinitions;
	private final String postRangeQuery;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preRangeQuery).append("[");
		sb.append(startQueryDefinitions);
		sb.append(" to ");
		sb.append(endQueryDefinitions);
		sb.append("]").append(postRangeQuery);
		return sb.toString();
	}

	public DslRangeQueryDefinition(final String preRangeQuery, final DslQueryDefinition startQueryDefinitions, final DslQueryDefinition endQueryDefinitions, final String postRangeQuery) {
		this.preRangeQuery = preRangeQuery;
		this.startQueryDefinitions = startQueryDefinitions;
		this.endQueryDefinitions = endQueryDefinitions;
		this.postRangeQuery = postRangeQuery;
	}

	/**
	 * @return preRangeQuery
	 */
	public final String getPreRangeQuery() {
		return preRangeQuery;
	}

	/**
	 * @return startQueryDefinitions
	 */
	public final DslQueryDefinition getStartQueryDefinitions() {
		return startQueryDefinitions;
	}

	/**
	 * @return endQueryDefinitions
	 */
	public final DslQueryDefinition getEndQueryDefinitions() {
		return endQueryDefinitions;
	}

	/**
	 * @return postRangeQuery
	 */
	public final String getPostRangeQuery() {
		return postRangeQuery;
	}

}
