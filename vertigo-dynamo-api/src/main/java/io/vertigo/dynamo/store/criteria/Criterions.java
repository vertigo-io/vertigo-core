package io.vertigo.dynamo.store.criteria;

import java.util.function.Predicate;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;

/**
 *
 * This class provides criterions (aka where clause) for a field of an entity.
 *
 * @author pchretien
 *
 */
public final class Criterions {
	private Criterions() {
		//stateless
	}

	/**
	 * @param dtFieldName the field
	 * @return is null
	 */
	public static <E extends Entity> Criteria<E> isNull(final DtFieldName<E> dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not null
	 */
	public static <E extends Entity> Criteria<E> isNotNull(final DtFieldName<E> dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NOT_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isEqualTo(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.EQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isNotEqualTo(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.NEQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isGreaterThan(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than or equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isGreaterThanOrEqualTo(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isLessThan(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than or equal to the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> isLessThanOrEqualTo(final DtFieldName<E> dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return starts with the value
	 * @param value the value
	 */
	public static <E extends Entity> Criteria<E> startsWith(final DtFieldName<E> dtFieldName, final String value) {
		return new Criterion<>(dtFieldName, CriterionOperator.STARTS_WITH, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is between min and max
	 * @param min the min value
	 * @param max the max value
	 */
	public static <E extends Entity> Criteria<E> isBetween(final DtFieldName<E> dtFieldName, final Comparable min, final Comparable max) {
		return new Criterion<>(dtFieldName, CriterionOperator.BETWEEN, min, max);
	}

	/**
	 * @param dtFieldName the field
	 * @return is in a list of values
	 * @param values list of allowed values
	 */
	public static <E extends Entity> Criteria<E> in(final DtFieldName<E> dtFieldName, final Comparable... values) {
		return new Criterion<>(dtFieldName, CriterionOperator.IN, values);
	}

	/**
	 * An always true criteria.
	 * @return true
	 */
	public static <E extends Entity> Criteria<E> alwaysTrue() {
		return new AlwaysTrueCriteria<>();
	}

	private static class AlwaysTrueCriteria<E extends Entity> extends Criteria<E> {

		private static final long serialVersionUID = 2967018427662007659L;

		@Override
		public Predicate<E> toPredicate() {
			return entity -> true;
		}

		@Override
		String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect) {
			return "1=1";
		}
	}
}
