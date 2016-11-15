package io.vertigo.dynamo.store.criteria2;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;

public interface Criterions<E extends Entity> extends DtFieldName {

	public default Criterion<E> isNull() {
		return new Criterion<>(this, CriterionOperator.isNull);
	}

	public default Criterion<E> isNotNull() {
		return new Criterion<>(this, CriterionOperator.isNotNull);
	}

	public default Criterion<E> isEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.eq, value);
	}

	public default Criterion<E> isNotEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.neq, value);
	}

	public default Criterion<E> isGreaterThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gt, value);
	}

	public default Criterion<E> isGreaterThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gte, value);
	}

	public default Criterion<E> isLessThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lt, value);
	}

	public default Criterion<E> isLessThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lte, value);
	}

	public default Criterion<E> startsWith(final String value) {
		return new Criterion<>(this, CriterionOperator.startsWith, value);
	}

	public default Criterion<E> isBetween(final Comparable min, final Comparable max) {
		return new Criterion<>(this, CriterionOperator.between, min, max);
	}
}
