package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

public final class DslTermDefinition {
	//#(preBody)(termField)(postBody)#!\((defaultValue)\)
	private final String preBody;
	private final String termField;
	private final String postBody;
	private final Option<String> defaultValue;

	@Override
	public String toString() {
		return "#" + preBody + termField + postBody + "#" + (defaultValue.isDefined() ? "!(" + defaultValue.get() + ")" : "");
	}

	/**
	 * @param preBody String before body
	 * @param termField Term field (criteria's field)
	 * @param postBody String after body
	 * @param defaultValue Optional default value (used if null or empty criteria)
	 */
	public DslTermDefinition(final String preBody, final String termField, final String postBody, final Option<String> defaultValue) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(termField);
		Assertion.checkNotNull(postBody);
		Assertion.checkNotNull(defaultValue);
		//-----
		this.preBody = preBody;
		this.termField = termField;
		this.postBody = postBody;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return termField
	 */
	public final String getTermField() {
		return termField;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

	/**
	 * @return defaultValue
	 */
	public final Option<String> getDefaultValue() {
		return defaultValue;
	}
}
