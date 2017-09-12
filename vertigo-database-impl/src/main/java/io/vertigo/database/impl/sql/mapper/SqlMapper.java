package io.vertigo.database.impl.sql.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.database.sql.mapper.SqlAdapter;
import io.vertigo.lang.Assertion;

public final class SqlMapper {
	private final Map<Class, SqlAdapter> adaptersByJavaType;

	public SqlMapper(final List<SqlAdapter> adapters) {
		Assertion.checkNotNull(adapters);
		//---
		adaptersByJavaType = adapters
				.stream()
				.collect(Collectors.toMap(SqlAdapter::getJavaDataType, i -> i));
	}

	public Class getSqlType(final Class javaType) {
		return adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).getSqlDataType() : javaType;
	}

	// mail from ResultSet contains a String, dataType=Mail
	public <J> J toJava(final Class<J> javaType, final Object sqlValue) {
		return (J) (adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).toJava(sqlValue) : sqlValue);
	}

	// javaValue =Mail
	//-> String
	public Object toSql(final Class javaType, final Object javaValue) {
		return adaptersByJavaType.containsKey(javaType) ? adaptersByJavaType.get(javaType).toSql(javaValue) : javaValue;
	}

}
