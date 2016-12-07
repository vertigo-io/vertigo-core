package io.vertigo.dynamo.store.criteria2;

import java.util.function.Predicate;

import io.vertigo.dynamo.domain.model.Entity;

public interface CriteriaBool<E extends Entity> {
	default CriteriaExpression<E> and(final CriteriaBool<E> criterion) {
		return CriteriaExpression.and(this, criterion);
	}

	default CriteriaExpression<E> or(final CriteriaBool<E> criterion) {
		return CriteriaExpression.or(this, criterion);
	}

	Predicate<E> toPredicate();

	String toSql(final Ctx ctx);
}
