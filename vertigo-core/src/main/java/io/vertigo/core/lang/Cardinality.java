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
 * Represents relationship cardinalities in data models.
 * Provides symbolic notation (?, 1, *) and validation methods.
 * Used for defining associations between entities.
 */
public enum Cardinality {
	/** 
	 * Optional relationship (0 or 1).
	 * Symbol: ?
	 */
	OPTIONAL_OR_NULLABLE,
	/** 
	 * Mandatory single relationship (exactly 1).
	 * Symbol: 1
	 */
	ONE,
	/**
	 * Multiple relationship (0 to many).
	 * Symbol: *
	 */
	MANY;

	/**
	 * Checks if cardinality is optional.
	 * @return true if optional (0 or 1)
	 */
	public boolean isOptionalOrNullable() {
		return OPTIONAL_OR_NULLABLE == this;
	}

	/**
	 * Checks if cardinality is exactly one.
	 * @return true if exactly one
	 */
	public boolean hasOne() {
		return ONE == this;
	}

	/**
	 * Checks if cardinality allows multiple values.
	 * @return true if multiple values allowed
	 */
	public boolean hasMany() {
		return MANY == this;
	}

	/**
	 * Creates cardinality from symbol.
	 * @param sCardinality Symbol (?, 1, *)
	 * @return Matching cardinality
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
	 * Converts cardinality to symbol.
	 * @return Symbol (?, 1, *)
	 */
	public String toSymbol() {
		return switch (this) {
			case OPTIONAL_OR_NULLABLE -> "?";
			case ONE -> "1";
			case MANY -> "*";
		};
	}

}
