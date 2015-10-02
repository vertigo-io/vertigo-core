package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * Range query definition.
 * (preBody)\[(termQuery|fixedQuery) to (termQuery|fixedQuery)\](postBody)
 * @author npiedeloup
 */
public final class DslRangeQueryDefinition implements DslQueryDefinition {
	private final String preBody;
	private final DslQueryDefinition startQueryDefinitions;
	private final DslQueryDefinition endQueryDefinitions;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param startQueryDefinitions Start query
	 * @param endQueryDefinitions End query
	 * @param postBody String after body
	 */
	public DslRangeQueryDefinition(final String preBody, final DslQueryDefinition startQueryDefinitions, final DslQueryDefinition endQueryDefinitions, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(startQueryDefinitions);
		Assertion.checkNotNull(endQueryDefinitions);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.startQueryDefinitions = startQueryDefinitions;
		this.endQueryDefinitions = endQueryDefinitions;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(preBody).append("[")
				.append(startQueryDefinitions)
				.append(" to ")
				.append(endQueryDefinitions)
				.append("]").append(postBody)
				.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
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
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

}
