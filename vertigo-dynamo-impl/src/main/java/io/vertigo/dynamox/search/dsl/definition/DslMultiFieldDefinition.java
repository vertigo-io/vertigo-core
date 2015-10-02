package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Assertion;

import java.util.List;

/**
 * Multi fields definition.
 * (preBody)\[(fields)+,\](postBody)
 * @author npiedeloup
 */
public final class DslMultiFieldDefinition {
	private final String preBody;
	private final List<DslFieldDefinition> fields;
	private final String postBody;

	/**
	 * @param preBody String before body
	 * @param fields List of Index's fields
	 * @param postBody String after body
	 */
	public DslMultiFieldDefinition(final String preBody, final List<DslFieldDefinition> fields, final String postBody) {
		Assertion.checkNotNull(preBody);
		Assertion.checkNotNull(fields);
		Assertion.checkNotNull(postBody);
		//-----
		this.preBody = preBody;
		this.fields = fields;
		this.postBody = postBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()
				.append(preBody).append("[");
		String sep = "";
		for (final DslFieldDefinition field : fields) {
			sb.append(sep).append(field);
			sep = ",";
		}
		sb.append("]").append(postBody);
		return sb.toString();
	}

	/**
	 * @return preBody
	 */
	public final String getPreBody() {
		return preBody;
	}

	/**
	 * @return fields
	 */
	public final List<DslFieldDefinition> getFields() {
		return fields;
	}

	/**
	 * @return postBody
	 */
	public final String getPostBody() {
		return postBody;
	}
}
