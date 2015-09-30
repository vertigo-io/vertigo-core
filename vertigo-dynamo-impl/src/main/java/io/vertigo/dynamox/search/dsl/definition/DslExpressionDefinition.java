package io.vertigo.dynamox.search.dsl.definition;

import io.vertigo.lang.Option;

public final class DslExpressionDefinition {
	//private final static String PARSE_PATTERN_STRING = "(^?\\s*)(?:(\\w+)|(\\[[\\w,\\s]+\\]):)?([^\\s#]*)(?:#(\\S+)#(?:\\!\\((\\S+)\\))?(?:\\s+(?:to|TO|To)\\s+#(\\S+)#)?(?:\\!\\((\\S+)\\))?)?([^\\s#]*)(\\s|$)+";
	//private final static String PARSE_PATTERN_STRING = "(\\s*)(?:(\\S+)|(\\[[\\w,\\s]+\\]):)?(?:(\\S+)|(\\[.+\\])|(\\(.+\\)):)(\\s*)";
	//private final static Pattern PARSE_QUERY_PATTERN = Pattern.compile(PARSE_PATTERN_STRING);

	//(preExpression)(field|multiField):(query|multiQuery)(postExpression)
	private final String preExpression; //Spaces like
	private final Option<DslFieldDefinition> field;
	private final Option<DslMultiFieldDefinition> multiField;

	private final DslQueryDefinition query;
	private final String postExpression; //Spaces like

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(preExpression);
		if (field.isDefined()) {
			sb.append(field.get());
			sb.append(":");
		}
		if (multiField.isDefined()) {
			sb.append(multiField.get());
			sb.append(":");
		}
		sb.append(query);
		sb.append(postExpression);
		return sb.toString();
	}

	public DslExpressionDefinition(final String preExpression,
			final Option<DslFieldDefinition> field, final Option<DslMultiFieldDefinition> multiField,
			final DslQueryDefinition query,
			final String postExpression) {
		this.preExpression = preExpression;
		this.field = field;
		this.multiField = multiField;
		this.query = query;
		this.postExpression = postExpression;
	}

	public final String getPreExpression() {
		return preExpression;
	}

	public final Option<DslFieldDefinition> getField() {
		return field;
	}

	public final Option<DslMultiFieldDefinition> getMultiField() {
		return multiField;
	}

	public final DslQueryDefinition getQuery() {
		return query;
	}

	public final String getPostExpression() {
		return postExpression;
	}

}
