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
package io.vertigo.dynamo.store.criteria;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

final class Criterion<E extends Entity> extends Criteria<E> {
	private static final long serialVersionUID = -7797854063455062775L;
	private final DtFieldName dtFieldName;
	private final CriterionOperator criterionOperator;
	private final Comparable[] values;

	Criterion(final DtFieldName dtFieldName, final CriterionOperator criterionOperator, final Comparable... values) {
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
				return dtFieldName.name() + " = #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case NEQ:
				return dtFieldName.name() + " != #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case GT:
				return dtFieldName.name() + " > #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case GTE:
				return dtFieldName.name() + " >= #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case LT:
				return dtFieldName.name() + " < #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case LTE:
				return dtFieldName.name() + " <= #" + ctx.attributeName(dtFieldName, values[0]) + "#";
			case BETWEEN:
				return "(" + dtFieldName.name() + " >= #" + ctx.attributeName(dtFieldName, values[0]) + "# and " + dtFieldName.name() + " <= #" + ctx.attributeName(dtFieldName, values[1]) + "# )";
			case STARTS_WITH:
				return dtFieldName.name() + " like  #" + ctx.attributeName(dtFieldName, values[0]) + "#" + sqlDialect.getConcatOperator() + "'%%'";
			case IN:
				final String paramValues = Stream.of(values)
						.map(Criterion::prepareSqlInArgument)
						.collect(Collectors.joining(", "));
				return dtFieldName.name() + " in(" + paramValues + ")";
			default:
				throw new IllegalAccessError();
		}
	}

	private static String prepareSqlInArgument(final Comparable value) {
		Assertion.checkArgument(
				value instanceof String
						|| value instanceof Integer
						|| value instanceof Long,
				"Only String,Long and Integers are allowed in a where in clause");
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
				return values[0].equals(value);
			case NEQ:
				return !values[0].equals(value);
			//with Comparable(s)
			case GT:
				return values[0].compareTo(value) < 0;
			case GTE:
				return values[0].compareTo(value) <= 0;
			case LT:
				return values[0].compareTo(value) > 0;
			case LTE:
				return values[0].compareTo(value) >= 0;
			case BETWEEN:
				return values[0].compareTo(value) <= 0 && values[1].compareTo(value) >= 0;
			//with String
			case STARTS_WITH:
				return String.class.cast(value).startsWith((String) values[0]);
			//with list of comparables
			case IN:
				return Arrays.asList(values).contains(value);
			default:
				throw new IllegalAccessError();
		}
	}
}
