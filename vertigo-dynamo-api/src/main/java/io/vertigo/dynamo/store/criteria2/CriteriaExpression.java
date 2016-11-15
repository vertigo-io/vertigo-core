package io.vertigo.dynamo.store.criteria2;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;

public final class CriteriaExpression<E extends Entity> implements CriteriaBool<E> {
	private enum CriteriaOperator {
		or, //(2),
		and;//(2);
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
	private final CriteriaBool<E>[] operands;

	private CriteriaExpression(final CriteriaOperator operator, final CriteriaBool<E>[] leftOperands, final CriteriaBool<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperands);
		//---
		this.operator = operator;
		final int size = leftOperands.length + 1;
		this.operands = new CriteriaBool[size];
		for (int i = 0; i < leftOperands.length; i++) {
			this.operands[i] = leftOperands[i];
		}
		this.operands[size - 1] = rightOperand;
	}

	private CriteriaExpression(final CriteriaOperator operator, final CriteriaBool<E> leftOperand, final CriteriaBool<E> rightOperand) {
		Assertion.checkNotNull(operator);
		Assertion.checkNotNull(leftOperand);
		//---
		this.operator = operator;
		this.operands = new CriteriaBool[] { leftOperand, rightOperand };
	}

	public CriteriaOperator getOperator() {
		return operator;
	}

	public CriteriaBool<E>[] getOperands() {
		return operands;
	}

	//=========================================================
	//=========================================================
	//=========================================================
	//Comment gérer la prorité des opérations ?
	public static <E extends Entity> CriteriaExpression<E> and(final CriteriaBool<E> leftOperand, final CriteriaBool<E> rightOperand) {
		//if exp*c
		//	when a*b*c
		//		then *(exp.operands, c)
		//	when a+b*c
		//		then +(exp.operands.left, *(exp.operands.left, c))
		if (leftOperand instanceof CriteriaExpression && rightOperand instanceof Criterion) {
			final CriteriaExpression<E> criteria = CriteriaExpression.class.cast(leftOperand);
			switch (criteria.getOperator()) {
				case and:
					return new CriteriaExpression<>(CriteriaOperator.and, criteria.operands, rightOperand);
				case or:
					//the most complex case !  a+b*c => a + (b*c)
					final CriteriaBool<E>[] leftOperands = new CriteriaBool[criteria.operands.length - 1];
					for (int i = 0; i < (criteria.operands.length - 1); i++) {
						leftOperands[i] = criteria.operands[i];
					}
					return new CriteriaExpression<>(CriteriaOperator.or, leftOperands, and(criteria.operands[criteria.operands.length - 1], rightOperand));
				default:
					throw new IllegalStateException();
			}
		}
		return new CriteriaExpression<>(CriteriaOperator.and, leftOperand, rightOperand);
	}

	public static <E extends Entity> CriteriaExpression<E> or(final CriteriaBool<E> leftOperand, final CriteriaBool<E> rightOperand) {
		//if exp+c
		//	when a*b+c
		//		then +(exp, c)
		//	when a+b+c
		//		then +(exp.operands, c)
		if (leftOperand instanceof CriteriaExpression && rightOperand instanceof Criterion) {
			final CriteriaExpression<E> criteria = CriteriaExpression.class.cast(leftOperand);
			if (criteria.getOperator() == CriteriaOperator.or) {
				return new CriteriaExpression<>(CriteriaOperator.or, criteria.operands, rightOperand);
			}
		}
		return new CriteriaExpression<>(CriteriaOperator.or, leftOperand, rightOperand);
	}

	@Override
	public Predicate<E> toPredicate() {
		final BinaryOperator<Predicate<E>> accumulator;
		if (operator == CriteriaOperator.or) {
			accumulator = Predicate::or;
		} else if (operator == CriteriaOperator.and) {
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
	public String toSql(final Ctx ctx) {
		return Arrays.stream(operands)
				.map(operand -> operand.toSql(ctx))
				.collect(Collectors.joining(" " + operator.name() + " ", "( ", " ) "));
	}
}
