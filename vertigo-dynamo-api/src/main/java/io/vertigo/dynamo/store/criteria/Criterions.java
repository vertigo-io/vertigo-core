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
	public static Criteria isNull(final DtFieldName dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not null
	 */
	public static Criteria isNotNull(final DtFieldName dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NOT_NULL);
	}

	/**
	 * @param dtFieldName the field
	 * @return is equal to the value
	 * @param value the value
	 */
	public static Criteria isEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.EQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is not equal to the value
	 * @param value the value
	 */
	public static Criteria isNotEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.NEQ, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than the value
	 * @param value the value
	 */
	public static Criteria isGreaterThan(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is greater than or equal to the value
	 * @param value the value
	 */
	public static Criteria isGreaterThanOrEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than the value
	 * @param value the value
	 */
	public static Criteria isLessThan(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LT, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is less than or equal to the value
	 * @param value the value
	 */
	public static Criteria isLessThanOrEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LTE, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return starts with the value
	 * @param value the value
	 */
	public static Criteria startsWith(final DtFieldName dtFieldName, final String value) {
		return new Criterion<>(dtFieldName, CriterionOperator.STARTS_WITH, value);
	}

	/**
	 * @param dtFieldName the field
	 * @return is between min and max
	 * @param min the min value
	 * @param max the max value
	 */
	public static Criteria isBetween(final DtFieldName dtFieldName, final Comparable min, final Comparable max) {
		return new Criterion<>(dtFieldName, CriterionOperator.BETWEEN, min, max);
	}

	/**
	 * @param dtFieldName the field
	 * @return is in a list of values
	 * @param values list of allowed values
	 */
	public static Criteria in(final DtFieldName dtFieldName, final Comparable... values) {
		return new Criterion<>(dtFieldName, CriterionOperator.IN, values);
	}

	/**
	 * An always true criteria.
	 * @return true
	 */
	public static Criteria alwaysTrue() {
		return new AlwaysTrueCriteria();
	}

	private static class AlwaysTrueCriteria extends Criteria<Entity> {

		private static final long serialVersionUID = 2967018427662007659L;

		@Override
		public Predicate<Entity> toPredicate() {
			return (entity) -> true;
		}

		@Override
		String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect) {
			return "1=1";
		}

	}
}
