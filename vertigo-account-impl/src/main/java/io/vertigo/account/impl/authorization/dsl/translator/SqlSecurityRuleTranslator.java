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
package io.vertigo.account.impl.authorization.dsl.translator;

import java.io.Serializable;
import java.util.List;

import io.vertigo.account.authorization.metamodel.rulemodel.DslExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.DslFixedValue;
import io.vertigo.account.authorization.metamodel.rulemodel.DslMultiExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.DslUserPropertyValue;

/**
 *
 *
 * @author npiedeloup
 */
public final class SqlSecurityRuleTranslator extends AbstractSecurityRuleTranslator<SqlSecurityRuleTranslator> {

	/**
	 * @return This security rule as SQL Query
	 */
	public String toSql() {
		return buildQueryString();
	}

	private static final String DEFAULT_BOOL_SEP = " OR ";

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		String sep = "";
		for (final DslMultiExpression multiExpressionDefinition : getMultiExpressions()) {
			query.append(sep);
			appendMultiExpression(query, multiExpressionDefinition);
			sep = DEFAULT_BOOL_SEP;
		}
		return query.toString()
				.replaceAll("^\\s+", "") //replace whitespaces at beginning of a line
				.replaceAll("\\s+$", "") //replace whitespaces at end of a line
				.replaceAll("\\s+", " "); // replace multiple whitespaces by space
	}

	private void appendMultiExpression(final StringBuilder query, final DslMultiExpression multiExpressionDefinition) {
		String sep = "";
		final String boolSep = " " + multiExpressionDefinition.getBoolOperator() + " ";
		if (multiExpressionDefinition.isBlock()) {
			query.append('(');
		}
		for (final DslExpression expression : multiExpressionDefinition.getExpressions()) {
			query.append(sep);
			appendExpression(query, expression);
			sep = boolSep;
		}
		for (final DslMultiExpression multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			query.append(sep);
			appendMultiExpression(query, multiExpression);
			sep = boolSep;
		}
		if (multiExpressionDefinition.isBlock()) {
			query.append(')');
		}
	}

	private void appendExpression(final StringBuilder query, final DslExpression expressionDefinition) {

		query.append(expressionDefinition.getFieldName());
		if (expressionDefinition.getValue() instanceof DslUserPropertyValue) {
			final DslUserPropertyValue userPropertyValue = (DslUserPropertyValue) expressionDefinition.getValue();
			final List<Serializable> userValues = getUserCriteria(userPropertyValue.getUserProperty());
			if (userValues.size() > 0) {
				if (userValues.size() == 1) {
					query
							.append(expressionDefinition.getOperator())
							.append(userValues.get(0));
				} else {
					query.append(" IN (");
					String inSep = "";
					for (final Serializable userValue : userValues) {
						query.append(inSep);
						query.append(userValue);
						inSep = ",";
					}
					query.append(')');
				}
			}
		} else if (expressionDefinition.getValue() instanceof DslFixedValue) {
			query
					.append(expressionDefinition.getOperator())
					.append(((DslFixedValue) expressionDefinition.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expressionDefinition.getValue().getClass().getName());
		}
	}

}
