package io.vertigo.dynamo.store.criteria;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;

/**
 * A criteria to filter a list.
 * A criteria can be translated to an SQL query and a Predicate for Java filtering
 * To create a Criteria use the class {@link Criterions}
 * @param <E> the type of entity filtered with this criteria
 * @author mlaroche
 */
public abstract class Criteria<E extends Entity> implements Serializable {
	private static final long serialVersionUID = -990254492823334724L;

	/**
	 * Return a new criteria composing the previous criteria and the provided one with a and operator.
	 * @param criteria the criteria to add
	 * @return the composed criteria
	 */
	public final Criteria<E> and(final Criteria<E> criteria) {
		return CriteriaUtil.and(this, criteria);
	}

	/**
	 * Return a new criteria composing the previous criteria and the provided one with a or operator.
	 * @param criteria the criteria to add
	 * @return the composed criteria
	 */
	public final Criteria<E> or(final Criteria<E> criteria) {
		return CriteriaUtil.or(this, criteria);
	}

	/**
	 * Translate the criteria to a Java predicate
	 * @return the predicate
	 */
	public abstract Predicate<E> toPredicate();

	abstract String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect);

	/**
	 * Translate the criteria to a SQL statement
	 * @param sqlDialect the dialect to use
	 * @return a tuple with the query and the sql params
	 */
	public Tuples.Tuple2<String, CriteriaCtx> toSql(final SqlDialect sqlDialect) {
		Assertion.checkNotNull(sqlDialect);
		//---
		final CriteriaCtx ctx = new CriteriaCtx();
		final String sql = this.toSql(ctx, sqlDialect);
		return Tuples.of(sql, ctx);

	}
}
