package io.vertigo.dynamo.store.criteria2;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;

public interface Criterions<E extends Entity> extends DtFieldName {

	default Criterion<E> isNull() {
		return new Criterion<>(this, CriterionOperator.isNull);
	}

	default Criterion<E> isNotNull() {
		return new Criterion<>(this, CriterionOperator.isNotNull);
	}

	default Criterion<E> isEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.eq, value);
	}

	default Criterion<E> isNotEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.neq, value);
	}

	default Criterion<E> isGreaterThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gt, value);
	}

	default Criterion<E> isGreaterThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gte, value);
	}

	default Criterion<E> isLessThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lt, value);
	}

	default Criterion<E> isLessThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lte, value);
	}

	default Criterion<E> startsWith(final String value) {
		return new Criterion<>(this, CriterionOperator.startsWith, value);
	}

	default Criterion<E> isBetween(final Comparable min, final Comparable max) {
		return new Criterion<>(this, CriterionOperator.between, min, max);
	}
}
