package io.vertigo.dynamox.search.dsl.definition;

public final class DslTermQueryDefinition implements DslQueryDefinition {
	//(preQuery)(term)(postQuery)
	private final String preQuery;
	private final DslTermDefinition term;
	private final String postQuery;

	@Override
	public String toString() {
		return preQuery + term + postQuery;
	}

	public DslTermQueryDefinition(final String preQuery, final DslTermDefinition term, final String postQuery) {
		this.preQuery = preQuery;
		this.term = term;
		this.postQuery = postQuery;
	}

	/**
	 * @return preQuery
	 */
	public final String getPreQuery() {
		return preQuery;
	}

	/**
	 * @return term
	 */
	public final DslTermDefinition getTerm() {
		return term;
	}

	/**
	 * @return postQuery
	 */
	public final String getPostQuery() {
		return postQuery;
	}
}
