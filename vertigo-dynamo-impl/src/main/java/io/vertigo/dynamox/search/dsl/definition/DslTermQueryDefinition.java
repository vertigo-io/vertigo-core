package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * Term query definition.
 * (preBody)(term)(postBody)
 * @author npiedeloup
 */
public final class DslTermQueryDefinition implements DslQueryDefinition {
	private final String preBody;
	private final DslTermDefinition term;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param term Term definition
	 * @param postBody String after body
	 */
	public DslTermQueryDefinition(final String preBody, final DslTermDefinition term, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(term);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.term = term;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + term + postBody;
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return term
	 */
	public final DslTermDefinition getTerm() {
		return term;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}
}
