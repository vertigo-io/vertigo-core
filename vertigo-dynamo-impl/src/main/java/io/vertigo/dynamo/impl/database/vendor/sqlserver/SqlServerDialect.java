package io.vertigo.dynamo.impl.database.vendor.sqlserver;

import io.vertigo.dynamo.database.vendor.SqlDialect;

final class SqlServerDialect implements SqlDialect {

	@Override
	public String getConcatOperator() {
		return " + ";
	}

}
