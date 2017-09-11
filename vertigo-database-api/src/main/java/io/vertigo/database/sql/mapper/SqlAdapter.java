package io.vertigo.database.sql.mapper;

/**
 * This class adapts a non compliant SQL java type to a compliant SQL java type.
 *
 *  Sql			<=========> Java
 *  SqlDataType <=Adapter=> JavaDataType)
 *
 * By default only a few types are allowed
 * 	- Integer, Long, Double, BigDecimal
 * 	- Boolean
 * 	- String
 *  - LocalDate, ZonedDateTime
 *  - DataStream
 *
 *  If you want to store another value type like 'mail' you have to define a specific adapter.
 *
 * @author pchretien
 * @param <J> JavaDataType (ex : Mail, Point)
 * @param <S> SqlDataType (ex : String, Integer)
 */
public interface SqlAdapter<J, S> {
	/**
	 * String -> Mail
	 * @param sqlValue the stored value
	 * @return the value transformed in POJO
	 */
	J toJava(S sqlValue);

	/**
	 * Mail -> String
	 * @param javaValue the pojo value
	 * @return the value transformed in native sql type
	 */
	S toSql(J javaValue);

	/**
	 * ex : Mail, Point...
	 * @return the JavaDataTyp
	 */
	Class<J> getJavaDataType();

	/**
	 * ex : String, Integer....
	 * @return the DataSqlType
	 */
	Class<S> getSqlDataType();
}
