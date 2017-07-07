/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.criteria;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.database.sql.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

final class Criterion<E extends Entity> extends Criteria<E> {
	private static final long serialVersionUID = -7797854063455062775L;
	private final DtFieldName dtFieldName;
	private final CriterionOperator criterionOperator;
	private final Serializable[] values;

	Criterion(final DtFieldName dtFieldName, final CriterionOperator criterionOperator, final Serializable... values) {
		Assertion.checkNotNull(dtFieldName);
		Assertion.checkNotNull(criterionOperator);
		Assertion.when(!CriterionOperator.IN.equals(criterionOperator))
				.check(() -> criterionOperator.getArity() == values.length, "Only {0} argument(s) functions are allowed for operator '{1}'",
						criterionOperator.getArity(),
						criterionOperator);
		Assertion.checkNotNull(values);
		//---
		this.criterionOperator = criterionOperator;
		this.dtFieldName = dtFieldName;
		this.values = values;
	}

	@Override
	String toSql(final CriteriaCtx ctx, final SqlDialect sqlDialect) {
		switch (criterionOperator) {
			case IS_NOT_NULL:
				return dtFieldName.name() + " is not null";
			case IS_NULL:
				return dtFieldName.name() + " is null";
			case EQ:
				if (values[0] == null) {
					return dtFieldName.name() + " is null ";
				}
				return dtFieldName.name() + " = #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case NEQ:
				if (values[0] == null) {
					return dtFieldName.name() + " is not null ";
				}
				return "(" + dtFieldName.name() + " is null or " + dtFieldName.name() + " != #" + ctx.attributeName(dtFieldName, values[0]) + "# )";
			case GT:
				return dtFieldName.name() + " > #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case GTE:
				return dtFieldName.name() + " >= #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case LT:
				return dtFieldName.name() + " < #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case LTE:
				return dtFieldName.name() + " <= #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case BETWEEN:
				final CriterionLimit min = CriterionLimit.class.cast(values[0]);
				final CriterionLimit max = CriterionLimit.class.cast(values[1]);
				final StringBuilder sql = new StringBuilder();
				if (min.isDefined()) {
					sql.append(dtFieldName.name())
							.append(min.isIncluded() ? " >= " : " > ")
							.append('#').append(ctx.attributeName(dtFieldName, min.getValue())).append('#');
				}
				if (max.isDefined()) {
					if (sql.length() > 0) {
						sql.append(" and ");
					}
					sql.append(dtFieldName.name())
							.append(max.isIncluded() ? " <= " : " < ")
							.append('#').append(ctx.attributeName(dtFieldName, max.getValue())).append('#');
				}
				return "( " + sql.toString() + " )";
			case STARTS_WITH:
				return dtFieldName.name() + " like  #" + ctx.attributeName(dtFieldName, values[0]) + "#" + sqlDialect.getConcatOperator() + "'%%'";
			case IN:
				return Stream.of(values)
						.map(Criterion::prepareSqlInArgument)
						.collect(Collectors.joining(", ", dtFieldName.name() + " in(", ")"));
			default:
				throw new IllegalAccessError();
		}
	}

	private static String prepareSqlInArgument(final Serializable value) {
		Assertion.checkArgument(
				value instanceof String
						|| value instanceof Integer
						|| value instanceof Long,
				"Only String,Long and Integers are allowed in a where in clause.");
		// we check to avoid sql injection without espacing and parametizing the statement
		Assertion.when(value instanceof String)
				.check(() -> ((String) value).matches("[A-Za-z0-9_]*"), "Only simple characters are allowed");
		// ---
		if (value instanceof String) {
			return "'" + value.toString() + "'";
		}
		return value.toString();
	}

	@Override
	public Predicate<E> toPredicate() {
		return this::test;
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
				return Objects.equals(value, values[0]);
			case NEQ:
				return !Objects.equals(value, values[0]);
			//with Comparable(s)
			case GT:
				if (values[0] == null || value == null) {
					return false;
				}
				return ((Comparable) values[0]).compareTo(value) < 0;
			case GTE:
				if (values[0] == null || value == null) {
					return false;
				}
				return ((Comparable) values[0]).compareTo(value) <= 0;
			case LT:
				if (values[0] == null || value == null) {
					return false;
				}
				return ((Comparable) values[0]).compareTo(value) > 0;
			case LTE:
				if (values[0] == null || value == null) {
					return false;
				}
				return ((Comparable) values[0]).compareTo(value) >= 0;
			case BETWEEN:
				if (value == null) {
					return false;
				}
				final CriterionLimit min = CriterionLimit.class.cast(values[0]);
				final CriterionLimit max = CriterionLimit.class.cast(values[1]);
				if (!min.isDefined() && !max.isDefined()) {
					return true;//there is no limit
				}
				boolean test = true;
				if (min.isDefined()) {
					if (min.isIncluded()) {
						test = test && min.getValue().compareTo(value) <= 0;
					} else {
						test = test && min.getValue().compareTo(value) < 0;
					}
				}
				if (max.isDefined()) {
					if (max.isIncluded()) {
						test = test && max.getValue().compareTo(value) >= 0;
					} else {
						test = test && max.getValue().compareTo(value) > 0;
					}
				}
				return test;
			//with String
			case STARTS_WITH:
				if (values[0] == null || value == null) {
					return false;
				}
				return String.class.cast(value).startsWith((String) values[0]);
			//with list of comparables
			case IN:
				return Arrays.asList(values).contains(value);
			default:
				throw new IllegalAccessError();
		}
	}
}
