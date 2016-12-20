package io.vertigo.dynamo.store.criteria2;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.store.criteria2.CriteriaExpression.CriteriaOperator;

final class CriteriaUtil {

	private CriteriaUtil() {
		//
	}

	//Comment gérer la prorité des opérations ?
	static <E extends Entity> Criteria2<E> and(final Criteria2<E> leftOperand, final Criteria2<E> rightOperand) {
		//if exp*c
		//	when a*b*c
		//		then *(exp.operands, c)
		//	when a+b*c
		//		then +(exp.operands.left, *(exp.operands.left, c))
		if (leftOperand instanceof CriteriaExpression && rightOperand instanceof Criterion) {
			final CriteriaExpression<E> criteria = CriteriaExpression.class.cast(leftOperand);
			switch (criteria.getOperator()) {
				case AND:
					return new CriteriaExpression<>(CriteriaOperator.AND, criteria.getOperands(), rightOperand);
				case OR:
					//the most complex case !  a+b*c => a + (b*c)
					final Criteria2<E>[] leftOperands = new Criteria2[criteria.getOperands().length - 1];
					for (int i = 0; i < (criteria.getOperands().length - 1); i++) {
						leftOperands[i] = criteria.getOperands()[i];
					}
					return new CriteriaExpression<>(CriteriaOperator.OR, leftOperands, and(criteria.getOperands()[criteria.getOperands().length - 1], rightOperand));
				default:
					throw new IllegalStateException();
			}
		}
		return new CriteriaExpression<>(CriteriaOperator.AND, leftOperand, rightOperand);
	}

	static <E extends Entity> Criteria2<E> or(final Criteria2<E> leftOperand, final Criteria2<E> rightOperand) {
		//if exp+c
		//	when a*b+c
		//		then +(exp, c)
		//	when a+b+c
		//		then +(exp.operands, c)
		if (leftOperand instanceof CriteriaExpression && rightOperand instanceof Criterion) {
			final CriteriaExpression<E> criteria = CriteriaExpression.class.cast(leftOperand);
			if (criteria.getOperator() == CriteriaOperator.OR) {
				return new CriteriaExpression<>(CriteriaOperator.OR, criteria.getOperands(), rightOperand);
			}
		}
		return new CriteriaExpression<>(CriteriaOperator.OR, leftOperand, rightOperand);
	}
}
