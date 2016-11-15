package io.vertigo.dynamo.store.criteria2;

public enum CriterionOperator {
	isNull(0),
	isNotNull(0),
	eq(1),
	neq(1),
	gt(1),
	gte(1),
	lt(1),
	lte(1),
	startsWith(1),
	between(2);

	private final int arity;

	private CriterionOperator(final int arity) {
		this.arity = arity;
	}

	public int getArity() {
		return arity;
	}

}
