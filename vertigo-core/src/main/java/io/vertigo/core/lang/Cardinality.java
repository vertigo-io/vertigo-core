/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
 * Defines relationship multiplicities in data models.
 * Supports three cardinality types:
 * - Optional (0..1) - Symbol: ?
 * - Mandatory (1) - Symbol: 1
 * - Multiple (0..*) - Symbol: *
 * 
 * Used for defining and validating entity associations.
 */
public enum Cardinality {
	/** 
	 * Optional relationship allowing zero or one instance.
	 * Used for nullable references.
	 * Symbol: ?
	 */
	OPTIONAL_OR_NULLABLE,
	/** 
	 * Mandatory relationship requiring exactly one instance.
	 * Used for required references.
	 * Symbol: 1
	 */
	ONE,
	/**
	 * Multiple relationship allowing zero or more instances.
	 * Used for collections and lists.
	 * Symbol: *
	 */
	MANY;

	/**
	 * Checks if relationship is optional.
	 * @return true for OPTIONAL_OR_NULLABLE cardinality
	 */
	public boolean isOptionalOrNullable() {
		return OPTIONAL_OR_NULLABLE == this;
	}

	/**
	 * Checks if relationship requires exactly one instance.
	 * @return true for ONE cardinality
	 */
	public boolean hasOne() {
		return ONE == this;
	}

	/**
	 * Checks if relationship allows multiple instances.
	 * @return true for MANY cardinality
	 */
	public boolean hasMany() {
		return MANY == this;
	}

	/**
	 * Parses cardinality from standard notation symbol.
	 * Supported symbols: ?, 1, *
	 *
	 * @param sCardinality Symbol to parse
	 * @return Corresponding cardinality value
	 * @throws VSystemException if symbol is invalid
	 */
	public static Cardinality fromSymbol(final String sCardinality) {
		Assertion.check().isNotBlank(sCardinality);
		//---
		return switch (sCardinality) {
			case "?" -> OPTIONAL_OR_NULLABLE;
			case "1" -> ONE;
			case "*" -> MANY;
			default -> throw new VSystemException("Unknown cardinality symbol : '{0}'. Supported cardinalities are '?' for optional, '1' for one and '*' for many ", sCardinality);
		};
	}

	/**
	 * Converts cardinality to standard notation symbol.
	 * @return Symbol (?, 1, or *)
	 */
	public String toSymbol() {
		return switch (this) {
			case OPTIONAL_OR_NULLABLE -> "?";
			case ONE -> "1";
			case MANY -> "*";
		};
	}

}
