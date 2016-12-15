package io.vertigo.dynamo.store.criteria2;

public enum CriterionOperator {
	IS_NULL(0),
	IS_NOT_NULL(0),
	EQ(1),
	NEQ(1),
	GT(1),
	GTE(1),
	LT(1),
	LTE(1),
	STARTS_WITH(1),
	BETWEEN(2);

	private final int arity;

	private CriterionOperator(final int arity) {
		this.arity = arity;
	}

	public int getArity() {
		return arity;
	}

}
