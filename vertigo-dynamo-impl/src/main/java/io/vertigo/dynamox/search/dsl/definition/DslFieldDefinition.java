package io.vertigo.dynamox.search.dsl.definition;

public final class DslFieldDefinition {
	//(preField)(fieldName)(postField)
	private final String preField;
	private final String fieldName;
	private final String postField;

	@Override
	public String toString() {
		return preField + fieldName + postField;
	}

	public DslFieldDefinition(final String preField, final String fieldName, final String postField) {
		this.preField = preField;
		this.fieldName = fieldName;
		this.postField = postField;
	}

	/**
	 * @return preField
	 */
	public final String getPreField() {
		return preField;
	}

	/**
	 * @return fieldName
	 */
	public final String getFieldName() {
		return fieldName;
	}

	/**
	 * @return postField
	 */
	public final String getPostField() {
		return postField;
	}

}
