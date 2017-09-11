package io.vertigo.database.impl.sql.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.database.sql.mapper.SqlAdapter;
import io.vertigo.database.sql.vendor.SqlMapping;
import io.vertigo.lang.Assertion;

public final class SqlMapper {
	private final Map<Class, SqlAdapter> adaptersByJavaType;

	public SqlMapper(final List<SqlAdapter> adapters) {
		Assertion.checkNotNull(adapters);
		//---
		this.adaptersByJavaType = adapters
				.stream()
				.collect(Collectors.toMap(SqlAdapter::getJavaDataType, i -> i));
	}

	private Class getSqlType(final Class javaType) {
		return adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).getSqlDataType() : javaType;
	}

	private <J> J toJava(final Class<J> javaType, final Object sqlValue) {
		return (J) (adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).toJava(sqlValue) : sqlValue);
	}

	private Object toSql(final Class javaType, final Object javaValue) {
		return adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).toSql(javaValue) : javaValue;
	}

	// javaValue =Mail
	//-> String
	public <J> void setValueOnStatement(
			final SqlMapping sqlMapping,
			final PreparedStatement statement,
			final int index,
			final Class<J> javaType,
			final J javaValue) throws SQLException {
		//we hae to translate a potential non supported type to a supported type
		final Object sqlValue = toSql(javaType, javaValue);
		final Class sqlType = getSqlType(javaType);
		sqlMapping.setValueOnStatement(statement, index, sqlType, sqlValue);
	}

	// mail from ResultSet contains a String, dataType=Mail
	public <J> J getValueForResultSet(
			final SqlMapping sqlMapping,
			final ResultSet resultSet,
			final int col,
			final Class<J> javaType) throws SQLException {
		final Class sqlType = getSqlType(javaType);
		final Object sqlValue = sqlMapping.getValueForResultSet(resultSet, col, sqlType);
		return toJava(javaType, sqlValue);
	}
}
