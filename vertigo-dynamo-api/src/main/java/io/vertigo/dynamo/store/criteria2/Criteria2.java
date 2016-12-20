package io.vertigo.dynamo.store.criteria2;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Tuples;

public abstract class Criteria2<E extends Entity> implements Serializable {
	private static final long serialVersionUID = -990254492823334724L;

	public final Criteria2<E> and(final Criteria2<E> criterion) {
		return CriteriaUtil.and(this, criterion);
	}

	public final Criteria2<E> or(final Criteria2<E> criterion) {
		return CriteriaUtil.or(this, criterion);
	}

	public abstract Predicate<E> toPredicate();

	abstract String toSql(final Ctx ctx);

	public Tuples.Tuple2<String, Ctx> toSql() {
		final Ctx ctx = new Ctx();
		final String sql = this.toSql(ctx);
		return new Tuples.Tuple2<>(sql, ctx);

	}
}
