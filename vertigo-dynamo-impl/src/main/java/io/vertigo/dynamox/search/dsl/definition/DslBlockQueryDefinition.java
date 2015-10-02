package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Block queries definition.
 * (preBody)\((query|rangeQuery|multiQuery|fixedQuery)+\)(postBody)
 * @author npiedeloup
 */
public final class DslBlockQueryDefinition implements DslQueryDefinition {
	private final String preBody;
	private final List<DslQueryDefinition> queries;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param queries List of queries
	 * @param postBody String after body
	 */
	public DslBlockQueryDefinition(final String preBody, final List<DslQueryDefinition> queries, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(queries);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.queries = queries;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(preBody).append("(");
		for (final DslQueryDefinition query : queries) {
			sb.append(query);
		}
		sb.append(")").append(postBody);
		return sb.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return queries
	 */
	public final List<DslQueryDefinition> getQueries() {
		return queries;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}
}
