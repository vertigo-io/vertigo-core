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
	default Criteria2<E> isNull() {
		return new Criterion<>(this, CriterionOperator.IS_NULL);
	}

	/**
	 * @return is not null
	 */
	default Criteria2<E> isNotNull() {
		return new Criterion<>(this, CriterionOperator.IS_NOT_NULL);
	}

	/**
	 * @return is equal to the value
	 * @param value the value
	 */
	default Criteria2<E> isEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.EQ, value);
	}

	/**
	 * @return is not equal to the value
	 * @param value the value
	 */
	default Criteria2<E> isNotEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.NEQ, value);
	}

	/**
	 * @return is greater than the value
	 * @param value the value
	 */
	default Criteria2<E> isGreaterThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.GT, value);
	}

	/**
	 * @return is greater than or equal to the value
	 * @param value the value
	 */
	default Criteria2<E> isGreaterThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.GTE, value);
	}

	/**
	 * @return is less than the value
	 * @param value the value
	 */
	default Criteria2<E> isLessThan(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.LT, value);
	}

	/**
	 * @return is less than or equal to the value
	 * @param value the value
	 */
	default Criteria2<E> isLessThanOrEqualTo(final Comparable value) {
		return new Criterion<>(this, CriterionOperator.LTE, value);
	}

	/**
	 * @return starts with the value
	 * @param value the value
	 */

	default Criteria2<E> startsWith(final String value) {
		return new Criterion<>(this, CriterionOperator.STARTS_WITH, value);
	}

	/**
	 * @return is between min and max
	 * @param min the min value
	 * @param max the max value
	 */
	default Criteria2<E> isBetween(final Comparable min, final Comparable max) {
		return new Criterion<>(this, CriterionOperator.BETWEEN, min, max);
	}
}
