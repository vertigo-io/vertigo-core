package io.vertigo.dynamo.store.criteria2;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;

/**
 *
 * This class provides criterions (aka where clause) for a field of an entity.
 *
 * @author pchretien
 *
 * @param <E> the type of entity to test
 */
public interface Criterions<E extends Entity> extends DtFieldName {

	/**
	 * @return is null
	 */
	default Criterion<E> isNull() {
		return new Criterion<>(this, CriterionOperator.isNull);
	}

	/**
	 * @return is not null
	 */
	default Criterion<E> isNotNull() {
		return new Criterion<>(this, CriterionOperator.isNotNull);
	}

	/**
	 * @return is equal to the value
	 */
	default Criterion<E> isEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.eq, value);
	}

	/**
	 * @return is not equal to the value
	 */
	default Criterion<E> isNotEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.neq, value);
	}

	/**
	 * @return is greater than the value
	 */
	default Criterion<E> isGreaterThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gt, value);
	}

	/**
	 * @return is greater than or equal to the value
	 */
	default Criterion<E> isGreaterThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.gte, value);
	}

	/**
	 * @return is less than the value
	 */
	default Criterion<E> isLessThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lt, value);
	}

	/**
	 * @return is less than or equal to the value
	 */
	default Criterion<E> isLessThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.lte, value);
	}

	/**
	 * @return starts with the value
	 */

	default Criterion<E> startsWith(final String value) {
		return new Criterion<>(this, CriterionOperator.startsWith, value);
	}

	/**
	 * @return is between min and max
	 */
	default Criterion<E> isBetween(final Comparable min, final Comparable max) {
		return new Criterion<>(this, CriterionOperator.between, min, max);
	}
}
