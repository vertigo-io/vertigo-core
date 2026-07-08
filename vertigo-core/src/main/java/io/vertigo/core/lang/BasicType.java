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

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Core type system for Vertigo platform.
 * Defines fundamental data types with built-in:
 * - Primitive and wrapper types (Integer, Double, Boolean)
 * - Common objects (String, BigDecimal)
 * - Date/Time types (LocalDate, Instant)
 * - Stream handling (DataStream)
 * 
 * Supports type categorization and validation.
 *
 * @author pchretien
 */
public enum BasicType {
	/** Integer type. */
	Integer(Integer.class),
	/** Double type. */
	Double(Double.class),
	/** Boolean type. */
	Boolean(Boolean.class),
	/** String type. */
	String(String.class),
	/** LocalDate type. */
	LocalDate(LocalDate.class),
	/** Instant type. */
	Instant(Instant.class),
	/** BigDecimal type. */
	BigDecimal(java.math.BigDecimal.class),
	/** Long type. */
	Long(Long.class),
	/** DataStream type. */
	DataStream(DataStream.class);

	/**
	 * The java class wrapped by this basic type.
	 */
	private final Class<?> javaClass;

	/**
	 * Creates a basic type with its Java class.
	 * @param javaClass Corresponding Java class
	 */
	BasicType(final Class<?> javaClass) {
		Assertion.check().isNotNull(javaClass);
		//-----
		this.javaClass = javaClass;
	}

	/**
	 * Checks if type represents date/time data.
	 * @return true for LocalDate and Instant types
	 */
	public boolean isAboutDate() {
		return this == BasicType.LocalDate
				|| this == BasicType.Instant;
	}

	/**
	 * Checks if type represents numeric data.
	 * @return true for Integer, Double, BigDecimal and Long types
	 */
	public boolean isNumber() {
		return this == BasicType.Double
				|| this == BasicType.BigDecimal
				|| this == BasicType.Long
				|| this == BasicType.Integer;
	}

	/**
	 * Static map for O(1) lookup of Java class to BasicType.
	 * Includes both wrapper and primitive types.
	 */
	private static final Map<Class<?>, BasicType> CLASS_TO_TYPE = Map.ofEntries(
			Map.entry(Integer.class, BasicType.Integer),
			Map.entry(int.class, BasicType.Integer),
			Map.entry(Double.class, BasicType.Double),
			Map.entry(double.class, BasicType.Double),
			Map.entry(Boolean.class, BasicType.Boolean),
			Map.entry(boolean.class, BasicType.Boolean),
			Map.entry(String.class, BasicType.String),
			Map.entry(LocalDate.class, BasicType.LocalDate),
			Map.entry(Instant.class, BasicType.Instant),
			Map.entry(java.math.BigDecimal.class, BasicType.BigDecimal),
			Map.entry(Long.class, BasicType.Long),
			Map.entry(long.class, BasicType.Long),
			Map.entry(DataStream.class, BasicType.DataStream));

	/**
	 * Gets the Java class for this type.
	 * @return Corresponding Java class
	 */
	public Class<?> getJavaClass() {
		return javaClass;
	}

	/**
	 * Finds BasicType for a Java class.
	 * Handles both primitive and wrapper types.
	 *
	 * @param type Java class to check
	 * @return Optional containing matching BasicType if found
	 */
	public static Optional<BasicType> of(final Class<?> type) {
		Assertion.check().isNotNull(type);
		//---
		return Optional.ofNullable(CLASS_TO_TYPE.get(type));
	}
}
