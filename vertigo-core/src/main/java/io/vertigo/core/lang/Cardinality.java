package io.vertigo.core.lang;

/** Cardinalities. */
public enum Cardinality {
	/** 
	 * card   : 0  or 1
	 * symbol : ?
	 */
	OPTIONAL_OR_NULLABLE,
	/** 
	 * card   : 1 
	 * symbol : 1
	 */
	ONE,
	/**
	 * card   :  0..n 
	 * symbol : *
	 */
	MANY;

	public boolean isOptionalOrNullable() {
		return OPTIONAL_OR_NULLABLE.equals(this);
	}

	public boolean hasOne() {
		return ONE.equals(this);
	}

	public boolean hasMany() {
		return MANY.equals(this);
	}

	public static Cardinality fromSymbol(final String sCardinality) {
		Assertion.checkArgNotEmpty(sCardinality);
		//---
		switch (sCardinality) {
			case "?":
				return OPTIONAL_OR_NULLABLE;
			case "1":
				return ONE;
			case "*":
				return MANY;
			default:
				throw new VSystemException("Unknown cardinality symbol : '" + sCardinality + "' Supported cardinalities are '?' for optional, '1' for one and '*' for many ");
		}
	}

	public String toSymbol() {
		switch (this) {
			case OPTIONAL_OR_NULLABLE:
				return "?";
			case ONE:
				return "1";
			case MANY:
				return "*";
			default:
				throw new VSystemException("Unknown cardinality : '" + this + "' Supported cardinalities are optional, one and many ");
		}
	}

}
