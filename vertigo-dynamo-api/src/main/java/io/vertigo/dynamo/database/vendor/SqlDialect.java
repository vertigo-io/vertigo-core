package io.vertigo.dynamo.database.vendor;

public interface SqlDialect {
	/**
	 * @return The operator for string concatenation.
	 */
	default String getConcatOperator() {
		return " || ";
	}
}
