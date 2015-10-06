package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * Range query definition.
 * (preBody)[\[\{](termQuery|fixedQuery) to (termQuery|fixedQuery)[\}\]](postBody)
 * @author npiedeloup
 */
public final class DslRangeQueryDefinition implements DslQueryDefinition {
	private final String preBody;
	private final String startRange;
	private final DslQueryDefinition startQueryDefinitions;
	private final DslQueryDefinition endQueryDefinitions;
	private final String endRange;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param startQueryDefinitions Start query
	 * @param endQueryDefinitions End query
	 * @param postBody String after body
	 */
	public DslRangeQueryDefinition(final String preBody, final String startRange,
			final DslQueryDefinition startQueryDefinitions, final DslQueryDefinition endQueryDefinitions,
			final String endRange, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkArgNotEmpty(startRange);
		Assertion.checkNotNull(startQueryDefinitions);
		Assertion.checkNotNull(endQueryDefinitions);
		Assertion.checkArgNotEmpty(endRange);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.startRange = startRange;
		this.startQueryDefinitions = startQueryDefinitions;
		this.endQueryDefinitions = endQueryDefinitions;
		this.endRange = endRange;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(preBody).append(startRange)
				.append(startQueryDefinitions)
				.append(" to ")
				.append(endQueryDefinitions)
				.append(endRange).append(postBody)
				.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return startRange
	 */
	public final String getStartRange() {
		return startRange;
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
	 * @return endRange
	 */
	public final String getEndRange() {
		return endRange;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

}
