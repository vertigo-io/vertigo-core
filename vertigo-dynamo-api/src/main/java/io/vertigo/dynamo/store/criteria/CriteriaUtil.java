package io.vertigo.dynamo.store.criteria;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.store.criteria.CriteriaExpression.CriteriaOperator;

final class CriteriaUtil {

	private CriteriaUtil() {
		//
	}

	//Comment gérer la prorité des opérations ?
	static <E extends Entity> Criteria<E> and(final Criteria<E> leftOperand, final Criteria<E> rightOperand) {
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
					final Criteria<E>[] leftOperands = new Criteria[criteria.getOperands().length - 1];
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

	static <E extends Entity> Criteria<E> or(final Criteria<E> leftOperand, final Criteria<E> rightOperand) {
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
