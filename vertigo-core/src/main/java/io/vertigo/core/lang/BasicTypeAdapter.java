/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.lang;

/**
 * Type adapter for converting between basic and custom Java types.
 * Enables storage and handling of custom types by mapping them to supported basic types:
 * - Numbers (Integer, Long, Double, BigDecimal)
 * - Boolean values
 * - Text (String)
 * - Dates (LocalDate, Instant)
 * - Binary data (DataStream)
 * 
 * Example: EmailAddress (custom) <-> String (basic)
 * 
 * Adapters must handle null values correctly.
 *
 * @author pchretien
 * @param <J> Custom Java type (e.g. EmailAddress, GeoPoint)
 * @param <B> Basic type for storage (e.g. String, Integer)
 */
public interface BasicTypeAdapter<J, B> {
	/**
	 * Converts from basic type to custom Java type.
	 * 
	 * @param basicValue Value in basic type format
	 * @param javaType Target Java class
	 * @return Converted value in custom type
	 */
	J toJava(B basicValue, Class<J> javaType);

	/**
	 * Converts from custom Java type to basic type.
	 * 
	 * @param javaValue Custom type value (may be null)
	 * @return Value converted to basic type
	 */
	B toBasic(J javaValue);

	/**
	 * Gets the basic type used for storage.
	 * 
	 * @return Basic type for this adapter
	 */
	BasicType getBasicType();

}
