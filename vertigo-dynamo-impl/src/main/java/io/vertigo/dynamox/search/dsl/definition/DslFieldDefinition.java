package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

/**
 * Single field definition.
 * (preBody)(fieldName)(postBody)
 * @author npiedeloup
 */
public final class DslFieldDefinition {
	private final String preBody;
	private final String fieldName;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fieldName Index's fieldName
	 * @param postBody String after body
	 */
	public DslFieldDefinition(final String preBody, final String fieldName, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fieldName = fieldName;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return preBody + fieldName + postBody;
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return fieldName
	 */
	public final String getFieldName() {
		return fieldName;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}

}
