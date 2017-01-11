package io.vertigo.dynamo.store.criteria;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

final class CriteriaExpression<E extends Entity> extends Criteria<E> {
	private static final long serialVersionUID = 8301054336845536973L;

	enum CriteriaOperator {
		OR, //(2),
		AND;//(2);
		//	not(1);
		//---
		//		private final int arity;
		//
		//		CriteriaOperator(final int arity) {
		//			this.arity = arity;
		//		}
		//
		//		public int getArity() {
		//			return arity;
		//		}
		//		Assertion.checkArgument(operands.length >= operator.getArity(), "the function {0} has a min arity of {1}", operator, operator.getArity());
	}

	private final CriteriaOperator operator;
	private final Criteria<E>[] operands;

	CriteriaExpression(final CriteriaOperator operator, final Criteria<E>[] leftOperands, final Criteria<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperands);
		//---
		this.operator = operator;
		final int size = leftOperands.length + 1;
		this.operands = new Criteria[size];
		for (int i = 0; i < leftOperands.length; i++) {
			this.operands[i] = leftOperands[i];
		}
		this.operands[size - 1] = rightOperand;
	}

	CriteriaExpression(final CriteriaOperator operator, final Criteria<E> leftOperand, final Criteria<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperand);
		//---
		this.operator = operator;
		this.operands = new Criteria[] { leftOperand, rightOperand };
	}

	CriteriaOperator getOperator() {
		return operator;
	}

	Criteria<E>[] getOperands() {
		return operands;
	}

	@Override
	public Predicate<E> toPredicate() {
		final BinaryOperator<Predicate<E>> accumulator;
		if (operator == CriteriaOperator.OR) {
			accumulator = Predicate::or;
		} else if (operator == CriteriaOperator.AND) {
			accumulator = Predicate::and;
		} else {
			throw new IllegalAccessError();
		}

		return Arrays.stream(operands)
				.map(operand -> operand.toPredicate())
				.reduce(accumulator)
				.orElseThrow(() -> new IllegalAccessError());
	}

	@Override
	String toSql(final CriteriaCtx ctx, final SqlDataBase sqlDataBase) {
		return Arrays.stream(operands)
				.map(operand -> operand.toSql(ctx, sqlDataBase))
				.collect(Collectors.joining(" " + operator.name() + " ", "( ", " ) "));
	}
}
