/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.persona.impl.security.dsl.model.DslExpression;
import io.vertigo.persona.impl.security.dsl.model.DslFixedValue;
import io.vertigo.persona.impl.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.impl.security.dsl.model.DslMultiExpression.BoolOperator;
import io.vertigo.persona.impl.security.dsl.model.DslUserPropertyValue;

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
		String boolSep;
		if (multiExpressionDefinition.getBoolOperator() == BoolOperator.AND) {
			boolSep = " +";
		} else {
			boolSep = " ";
		}

		for (final DslExpression expression : multiExpressionDefinition.getExpressions()) {
			query.append(sep).append('(');
			appendExpression(query, expression);
			query.append(')');
			sep = boolSep;
		}
		for (final DslMultiExpression multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			query.append(sep).append('(');
			appendMultiExpression(query, multiExpression);
			query.append(')');
			sep = boolSep;
		}
	}

	private void appendExpression(final StringBuilder query, final DslExpression expressionDefinition) {
		if (expressionDefinition.getValue() instanceof DslUserPropertyValue) {
			final DslUserPropertyValue userPropertyValue = (DslUserPropertyValue) expressionDefinition.getValue();
			final String[] userValues = getUserCriteria(userPropertyValue.getUserProperty());
			if (userValues != null && userValues.length > 0) {
				String inSep = "";
				for (final String userValue : userValues) {
					query.append(inSep)
							.append(expressionDefinition.getFieldName())
							.append(':')
							.append(userValue);
					inSep = " ";
				}
			}
		} else if (expressionDefinition.getValue() instanceof DslFixedValue) {
			query.append(expressionDefinition.getFieldName())
					.append(":")
					.append(((DslFixedValue) expressionDefinition.getValue()).getFixedValue());
		} else {
			throw new IllegalArgumentException("value type not supported " + expressionDefinition.getValue().getClass().getName());
		}
	}

}
