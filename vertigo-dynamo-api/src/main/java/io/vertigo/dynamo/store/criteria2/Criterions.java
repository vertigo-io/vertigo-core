package io.vertigo.dynamo.store.criteria2;

import java.util.function.Predicate;

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
	 * @return is null
	 */
	public static Criteria2 isNull(final DtFieldName dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NULL);
	}

	/**
	 * @return is not null
	 */
	public static Criteria2 isNotNull(final DtFieldName dtFieldName) {
		return new Criterion<>(dtFieldName, CriterionOperator.IS_NOT_NULL);
	}

	/**
	 * @return is equal to the value
	 * @param value the value
	 */
	public static Criteria2 isEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.EQ, value);
	}

	/**
	 * @return is not equal to the value
	 * @param value the value
	 */
	public static Criteria2 isNotEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.NEQ, value);
	}

	/**
	 * @return is greater than the value
	 * @param value the value
	 */
	public static Criteria2 isGreaterThan(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GT, value);
	}

	/**
	 * @return is greater than or equal to the value
	 * @param value the value
	 */
	public static Criteria2 isGreaterThanOrEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.GTE, value);
	}

	/**
	 * @return is less than the value
	 * @param value the value
	 */
	public static Criteria2 isLessThan(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LT, value);
	}

	/**
	 * @return is less than or equal to the value
	 * @param value the value
	 */
	public static Criteria2 isLessThanOrEqualTo(final DtFieldName dtFieldName, final Comparable value) {
		return new Criterion<>(dtFieldName, CriterionOperator.LTE, value);
	}

	/**
	 * @return starts with the value
	 * @param value the value
	 */

	public static Criteria2 startsWith(final DtFieldName dtFieldName, final String value) {
		return new Criterion<>(dtFieldName, CriterionOperator.STARTS_WITH, value);
	}

	/**
	 * @return is between min and max
	 * @param min the min value
	 * @param max the max value
	 */
	public static Criteria2 isBetween(final DtFieldName dtFieldName, final Comparable min, final Comparable max) {
		return new Criterion<>(dtFieldName, CriterionOperator.BETWEEN, min, max);
	}

	/**
	 * An always true criteria.
	 * @return true
	 */
	public static Criteria2 alwaysTrue() {
		return new AlwaysTrueCriteria();
	}

	private static class AlwaysTrueCriteria extends Criteria2<Entity> {

		private static final long serialVersionUID = 2967018427662007659L;

		@Override
		public Predicate<Entity> toPredicate() {
			return (entity) -> true;
		}

		@Override
		String toSql(final Ctx ctx) {
			return "1=1";
		}

	}
}
