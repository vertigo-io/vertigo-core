package io.vertigo.dynamox.search.dsl.definition;

import java.util.List;

public final class DslMultiFieldDefinition {
	//(preMultiField)[(fields)+,](postMultiField)
	private final String preMultiField;
	private final List<DslFieldDefinition> fields;
	private final String postMultiField;

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preMultiField).append("[");
		String sep = "";
		for (final DslFieldDefinition field : fields) {
			sb.append(sep).append(field);
			sep = ",";
		}
		sb.append("]").append(postMultiField);
		return sb.toString();
	}

	public DslMultiFieldDefinition(final String preMultiField, final List<DslFieldDefinition> fields, final String postMultiField) {
		this.preMultiField = preMultiField;
		this.fields = fields;
		this.postMultiField = postMultiField;
	}

	/**
	 * @return preMultiField
	 */
	public final String getPreMultiField() {
		return preMultiField;
	}

	/**
	 * @return fields
	 */
	public final List<DslFieldDefinition> getFields() {
		return fields;
	}

	/**
	 * @return postMultiField
	 */
	public final String getPostMultiField() {
		return postMultiField;
	}
}
