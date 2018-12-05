/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.account.authorization.metamodel.rulemodel.RuleExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleExpression.ValueOperator;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleFixedValue;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleMultiExpression.BoolOperator;
import io.vertigo.account.authorization.metamodel.rulemodel.RuleUserPropertyValue;

/**
 *
 *
 * @author npiedeloup
 */
public final class SearchSecurityRuleTranslator extends AbstractSecurityRuleTranslator<SearchSecurityRuleTranslator> {

	private static final String DEFAULT_BOOL_SEP = " ";

	/**
	 * @return This security rule as search Query
	 */
	public String toSearchQuery() {
		return buildQueryString();
	}

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		String sep = "";
		for (final RuleMultiExpression multiExpressionDefinition : getMultiExpressions()) {
			query.append(sep).append('(');//On ajoute cette parenthèse car le premier niveau de multiExpression est en OR
			appendMultiExpression(query, multiExpressionDefinition);
			query.append(')');
			sep = DEFAULT_BOOL_SEP;
		}
		return query.toString()
				.replaceAll("^\\s+", "") //replace whitespaces at beginning of a line
				.replaceAll("\\s+$", "") //replace whitespaces at end of a line
				.replaceAll("\\s+", " ") // replace multiple whitespaces by space
				.replaceAll("^\\(\\)$", "(*:*)") // replace empty query to all
		;
	}

	private void appendMultiExpression(final StringBuilder query, final RuleMultiExpression multiExpressionDefinition) {
		String sep = "";
		String boolSep;
		if (multiExpressionDefinition.getBoolOperator() == BoolOperator.AND) {
			boolSep = "+";
		} else {
			boolSep = "";
		}

		for (final RuleExpression expression : multiExpressionDefinition.getExpressions()) {
			query.append(sep).append(boolSep);
			appendExpression(query, expression);
			sep = " ";
		}
		for (final RuleMultiExpression multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			query.append(sep).append(boolSep).append('(');
			appendMultiExpression(query, multiExpression);
			query.append(')');
			sep = " ";
		}
	}

	private void appendExpression(final StringBuilder query, final RuleExpression expressionDefinition) {
		if (expressionDefinition.getOperator() == ValueOperator.NEQ) {
			query.append('-');
		}
		if (expressionDefinition.getValue() instanceof RuleUserPropertyValue) {
			final RuleUserPropertyValue userPropertyValue = (RuleUserPropertyValue) expressionDefinition.getValue();
			final List<Serializable> userValues = getUserCriteria(userPropertyValue.getUserProperty());
			final boolean useParenthesisAroundValue = userValues.size() > 1;
			if (!userValues.isEmpty()) {
				query.append(expressionDefinition.getFieldName())
						.append(':')
						.append(toOperator(expressionDefinition.getOperator()));
				if (useParenthesisAroundValue) {
					query.append('(');
				}

				String inSep = "";
				for (final Serializable userValue : userValues) {
					query.append(inSep)
							.append(userValue);
					inSep = " ";
				}
				if (useParenthesisAroundValue) {
					query.append(')');
				}
			}
		} else if (expressionDefinition.getValue() instanceof RuleFixedValue) {
			query.append(expressionDefinition.getFieldName())
					.append(':')
					.append(toOperator(expressionDefinition.getOperator()))
					.append(((RuleFixedValue) expressionDefinition.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expressionDefinition.getValue().getClass().getName());
		}
	}

	private static String toOperator(final ValueOperator operator) {
		switch (operator) {
			case GT:
				return ">";
			case GTE:
				return ">=";
			case LT:
				return "<";
			case LTE:
				return "<=";
			case EQ:
			case NEQ:
				return "";
			default:
				throw new IllegalArgumentException("Operator not supported " + operator.name());
		}
	}

}
