package io.vertigo.dynamo.criteria;

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.Entity;

/**
 * Defines a limit/boundary.
 * if limit is null then there is ... no limit
 * limit can be inclusive or exclusive.
 *
 * @author pchretien
 *
 * @param <E> the type of Entity
 */
public final class CriterionLimit<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 3903274923414317496L;

	private final Serializable value;
	private final boolean included;// else excluded

	public static <E extends Entity> CriterionLimit ofIncluded(final Serializable value) {
		return new CriterionLimit<>(value, true);
	}

	public static <E extends Entity> CriterionLimit ofExcluded(final Serializable value) {
		return new CriterionLimit<>(value, false);
	}

	private CriterionLimit(final Serializable value, final boolean included) {
		this.value = value;
		this.included = included;
	}

	boolean isDefined() {
		return value != null;
	}

	Comparable getValue() {
		return Comparable.class.cast(value);
	}

	boolean isIncluded() {
		return included;
	}
}
