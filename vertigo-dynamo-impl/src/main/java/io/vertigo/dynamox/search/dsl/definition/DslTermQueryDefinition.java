package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Term query definition.
 * (preBody)#(preBody)(termField)(postBody)#!\((defaultValue)\)(postBody)
 * @author npiedeloup
 */
public final class DslTermQueryDefinition implements DslQueryDefinition {
	private final String preBody;
	private final String preTerm;
	private final String termField;
	private final String postTerm;
	private final Option<String> defaultValue;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param preTerm String before body
	 * @param termField Term field (criteria's field)
	 * @param postBody String after body
	 * @param defaultValue Optional default value (used if null or empty criteria)
	 * @param postTerm String after body
	 */
	public DslTermQueryDefinition(final String preBody, final String preTerm, final String termField, final String postTerm, final Option<String> defaultValue, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(preTerm);
		Assertion.checkNotNull(termField);
		Assertion.checkNotNull(postTerm);
		Assertion.checkNotNull(defaultValue);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.preTerm = preTerm;
		this.termField = termField;
		this.postTerm = postTerm;
		this.defaultValue = defaultValue;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + "#" + preTerm + termField + postTerm + "#" + (defaultValue.isDefined() ? "!(" + defaultValue.get() + ")" : "") + postBody;
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return preTerm
	 */
	public final String getPreTerm() {
		return preTerm;
	}

	/**
	 * @return termField
	 */
	public final String getTermField() {
		return termField;
	}

	/**
	 * @return postTerm
	 */
	public final String getPostTerm() {
		return postTerm;
	}

	/**
	 * @return defaultValue
	 */
	public final Option<String> getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}
}
