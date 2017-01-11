package io.vertigo.dynamo.store.criteria;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Tuples;

public abstract class Criteria<E extends Entity> implements Serializable {
	private static final long serialVersionUID = -990254492823334724L;

	public final Criteria<E> and(final Criteria<E> criteria) {
		return CriteriaUtil.and(this, criteria);
	}

	public final Criteria<E> or(final Criteria<E> criteria) {
		return CriteriaUtil.or(this, criteria);
	}

	public abstract Predicate<E> toPredicate();

	abstract String toSql(final CriteriaCtx ctx, final SqlDataBase sqlDataBase);

	public Tuples.Tuple2<String, CriteriaCtx> toSql(final SqlDataBase sqlDataBase) {
		final CriteriaCtx ctx = new CriteriaCtx();
		final String sql = this.toSql(ctx, sqlDataBase);
		return new Tuples.Tuple2<>(sql, ctx);

	}
}
