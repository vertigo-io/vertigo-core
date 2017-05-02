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
package io.vertigo.persona.impl.security;

import java.io.Serializable;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.Criterions;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.dsl.model.DslExpression;
import io.vertigo.persona.security.dsl.model.DslExpression.ValueOperator;
import io.vertigo.persona.security.dsl.model.DslFixedValue;
import io.vertigo.persona.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.security.dsl.model.DslMultiExpression.BoolOperator;
import io.vertigo.persona.security.dsl.model.DslUserPropertyValue;

/**
 *
 *
 * @author npiedeloup
 */
public final class CriteriaSecurityRuleTranslator<E extends Entity> extends AbstractSecurityRuleTranslator<CriteriaSecurityRuleTranslator<E>> {

	/**
	 * @return This security rule as search Query
	 */
	public Criteria<E> toCriteria() {
		if (getMultiExpressions().isEmpty()) {
			return Criterions.alwaysTrue();
		}
		Criteria<E> mainCriteria = null;
		for (final DslMultiExpression expression : getMultiExpressions()) {
			final Criteria<E> criteria = toCriteria(expression);
			if (mainCriteria == null) {
				mainCriteria = criteria;
			} else {
				mainCriteria.or(criteria);
			}
		}
		Assertion.checkNotNull(mainCriteria);//can't be null
		return mainCriteria;
	}

	private Criteria<E> toCriteria(final DslMultiExpression multiExpression) {
		Criteria<E> firstCriteria = null;
		for (final DslExpression expression : multiExpression.getExpressions()) {
			final Criteria<E> criteria = toCriteria(expression);
			if (firstCriteria == null) {
				firstCriteria = criteria;
			} else {
				if (multiExpression.getBoolOperator() == BoolOperator.AND) {
					firstCriteria.and(toCriteria(expression));
				} else {
					firstCriteria.or(toCriteria(expression));
				}
			}
		}
		for (final DslMultiExpression expression : multiExpression.getMultiExpressions()) {
			final Criteria<E> criteria = toCriteria(expression);
			if (firstCriteria == null) {
				firstCriteria = criteria;
			} else {
				if (multiExpression.getBoolOperator() == BoolOperator.AND) {
					firstCriteria.and(toCriteria(expression));
				} else {
					firstCriteria.or(toCriteria(expression));
				}
			}
		}
		Assertion.checkNotNull(firstCriteria);//can be null ?
		return firstCriteria;
	}

	private Criteria<E> toCriteria(final DslExpression expression) {
		if (expression.getValue() instanceof DslUserPropertyValue) {
			final DslUserPropertyValue userPropertyValue = (DslUserPropertyValue) expression.getValue();
			final Serializable[] userValues = getUserCriteria(userPropertyValue.getUserProperty());
			if (userValues != null && userValues.length > 0) {
				Criteria<E> firstCriteria = null; //comment collecter en stream ?
				for (final Serializable userValue : userValues) {
					Assertion.checkNotNull(userValue);
					Assertion.checkArgument(userValue instanceof Comparable, "Security keys must be serializable AND comparable (here : {0})", userValues.getClass().getSimpleName());
					//----
					final Criteria<E> criteria = toCriteria(expression::getFieldName, expression.getOperator(), (Comparable<?>) userValue);
					if (firstCriteria == null) {
						firstCriteria = criteria;
					} else {
						firstCriteria.or(criteria);
					}
				}
				Assertion.checkNotNull(firstCriteria);//can't be null
				return firstCriteria;
			} else {
				return Criterions.alwaysFalse();
			}
		} else if (expression.getValue() instanceof DslFixedValue) {
			return toCriteria(expression::getFieldName, expression.getOperator(), ((DslFixedValue) expression.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expression.getValue().getClass().getName());
		}
	}

	private Criteria<E> toCriteria(final DtFieldName<E> fieldName, final ValueOperator operator, final Comparable<?> value) {
		switch (operator) {
			case EQ:
				return Criterions.isEqualTo(fieldName, value);
			case GT:
				return Criterions.isGreaterThan(fieldName, value);
			case GTE:
				return Criterions.isGreaterThanOrEqualTo(fieldName, value);
			case LT:
				return Criterions.isLessThan(fieldName, value);
			case LTE:
				return Criterions.isLessThanOrEqualTo(fieldName, value);
			case NEQ:
				return Criterions.isNotEqualTo(fieldName, value);
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

}
