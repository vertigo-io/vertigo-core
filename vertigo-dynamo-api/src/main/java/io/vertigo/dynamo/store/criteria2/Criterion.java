package io.vertigo.dynamo.store.criteria2;

import java.util.function.Predicate;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

public final class Criterion<E extends Entity> implements CriteriaBool<E> {
	private final DtFieldName dtFieldName;
	private final CriterionOperator criterionOperator;
	private final Comparable value1, value2;

	Criterion(final DtFieldName dtFieldName, final CriterionOperator criterionOperator) {
		Assertion.checkNotNull(dtFieldName);
		Assertion.checkNotNull(criterionOperator);
		Assertion.checkArgument(criterionOperator.getArity() == 0, "Only zero argument functions are allowed");
		//---
		this.criterionOperator = criterionOperator;
		this.dtFieldName = dtFieldName;
		value1 = null;
		value2 = null;
	}

	Criterion(final DtFieldName dtFieldName, final CriterionOperator criterionOperator, final Comparable value) {
		Assertion.checkNotNull(dtFieldName);
		Assertion.checkNotNull(criterionOperator);
		Assertion.checkArgument(criterionOperator.getArity() == 1, "Only one argument functions are allowed");
		Assertion.checkNotNull(value);
		//---
		this.criterionOperator = criterionOperator;
		this.dtFieldName = dtFieldName;
		value1 = value;
		value2 = null;
	}

	Criterion(final DtFieldName dtFieldName, final CriterionOperator criterionOperator, final Comparable value1, final Comparable value2) {
		Assertion.checkNotNull(dtFieldName);
		Assertion.checkNotNull(criterionOperator);
		Assertion.checkArgument(criterionOperator.getArity() == 2, "Only two arguments functions are allowed");
		Assertion.checkNotNull(value1);
		Assertion.checkNotNull(value2);
		//---
		this.criterionOperator = criterionOperator;
		this.dtFieldName = dtFieldName;
		this.value1 = value1;
		this.value2 = value2;
	}

	public CriterionOperator getOperator() {
		return criterionOperator;
	}

	public DtFieldName getDtFieldName() {
		return dtFieldName;
	}

	public Object getValue1() {
		return value1;
	}

	public Object getValue2() {
		return value2;
	}

	@Override
	public String toSql(final Ctx ctx) {
		switch (criterionOperator) {
			case isNotNull:
				return dtFieldName + " is not null";
			case isNull:
				return dtFieldName + " is null";
			case eq:

				return dtFieldName + " = #" + ctx.attributeName(dtFieldName, value1) + "#";
			case neq:
				return dtFieldName + " != #" + ctx.attributeName(dtFieldName, value1) + "#";
			case gt:
				return dtFieldName + " > #" + ctx.attributeName(dtFieldName, value1) + "#";
			case gte:
				return dtFieldName + " >= #" + ctx.attributeName(dtFieldName, value1) + "#";
			case lt:
				return dtFieldName + " < #" + ctx.attributeName(dtFieldName, value1) + "#";
			case lte:
				return dtFieldName + " <= #" + ctx.attributeName(dtFieldName, value1) + "#";
			case between:
				return "(" + dtFieldName + " >= #" + ctx.attributeName(dtFieldName, value1) + "# and " + dtFieldName + " <= #" + ctx.attributeName(dtFieldName, value2) + "# )";
			case startsWith:
				return dtFieldName + " like  #" + ctx.attributeName(dtFieldName, value1) + "# || " + "'%%'";
			default:
				throw new IllegalAccessError();
		}
	}

	@Override
	public Predicate<E> toPredicate() {
		return (final E entity) -> {
			final DtDefinition entitytDefinition = DtObjectUtil.findDtDefinition(entity.getClass());
			final Object value = entitytDefinition.getField(dtFieldName).getDataAccessor().getValue(entity);

			switch (criterionOperator) {
				case isNotNull:
					return value != null;
				case isNull:
					return value == null;
				case eq:
					return value1.equals(value);
				case neq:
					return value1 != value;
				case gt:
					return value1.compareTo(value) < 0;
				case gte:
					return value1.compareTo(value) <= 0;
				case lt:
					return value1.compareTo(value) > 0;
				case lte:
					return value1.compareTo(value) >= 0;
				case between:
					return value1.compareTo(value) <= 0 && value2.compareTo(value) >= 0;
				case startsWith:
					return String.class.cast(value).startsWith((String) value1);
				default:
					throw new IllegalAccessError();
			}
		};
	}
}
