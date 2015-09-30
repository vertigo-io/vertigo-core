package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Option;

public final class DslTermDefinition {
	//#(preTerm)(termField)(postTerm)#!\((defaultValue)\)
	private final String preTerm;
	private final String termField;
	private final String postTerm;
	private final Option<String> defaultValue;

	@Override
	public String toString() {
		return "#" + preTerm + termField + postTerm + "#" + (defaultValue.isDefined() ? "!(" + defaultValue.get() + ")" : "");
	}

	public DslTermDefinition(final String preTerm, final String termField, final String postTerm, final Option<String> defaultValue) {
		this.preTerm = preTerm;
		this.termField = termField;
		this.postTerm = postTerm;
		this.defaultValue = defaultValue;
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
}
