package io.vertigo.dynamo.store.criteria2;

import java.util.function.Predicate;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

final class Criterion<E extends Entity> extends Criteria2<E> {
	private static final long serialVersionUID = -7797854063455062775L;
	private final DtFieldName dtFieldName;
	private final CriterionOperator criterionOperator;
	private final Comparable value1;
	private final Comparable value2;

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

	@Override
	String toSql(final Ctx ctx) {
		switch (criterionOperator) {
			case IS_NOT_NULL:
				return dtFieldName.name() + " is not null";
			case IS_NULL:
				return dtFieldName.name() + " is null";
			case EQ:
				return dtFieldName.name() + " = #" + ctx.attributeName(dtFieldName, value1) + "#";
			case NEQ:
				return dtFieldName.name() + " != #" + ctx.attributeName(dtFieldName, value1) + "#";
			case GT:
				return dtFieldName.name() + " > #" + ctx.attributeName(dtFieldName, value1) + "#";
			case GTE:
				return dtFieldName.name() + " >= #" + ctx.attributeName(dtFieldName, value1) + "#";
			case LT:
				return dtFieldName.name() + " < #" + ctx.attributeName(dtFieldName, value1) + "#";
			case LTE:
				return dtFieldName.name() + " <= #" + ctx.attributeName(dtFieldName, value1) + "#";
			case BETWEEN:
				return "(" + dtFieldName.name() + " >= #" + ctx.attributeName(dtFieldName, value1) + "# and " + dtFieldName.name() + " <= #" + ctx.attributeName(dtFieldName, value2) + "# )";
			case STARTS_WITH:
				return dtFieldName.name() + " like  #" + ctx.attributeName(dtFieldName, value1) + "# || " + "'%%'";
			default:
				throw new IllegalAccessError();
		}
	}

	@Override
	public Predicate<E> toPredicate() {
		return entity -> test(entity);
	}

	private boolean test(final E entity) {
		final DtDefinition entitytDefinition = DtObjectUtil.findDtDefinition(entity.getClass());
		final Object value = entitytDefinition.getField(dtFieldName).getDataAccessor().getValue(entity);

		switch (criterionOperator) {
			case IS_NOT_NULL:
				return value != null;
			case IS_NULL:
				return value == null;
			case EQ:
				return value1.equals(value);
			case NEQ:
				return !value1.equals(value);
			//with Comparable(s)
			case GT:
				return value1.compareTo(value) < 0;
			case GTE:
				return value1.compareTo(value) <= 0;
			case LT:
				return value1.compareTo(value) > 0;
			case LTE:
				return value1.compareTo(value) >= 0;
			case BETWEEN:
				return value1.compareTo(value) <= 0 && value2.compareTo(value) >= 0;
			//with String
			case STARTS_WITH:
				return String.class.cast(value).startsWith((String) value1);
			default:
				throw new IllegalAccessError();
		}
	}
}
