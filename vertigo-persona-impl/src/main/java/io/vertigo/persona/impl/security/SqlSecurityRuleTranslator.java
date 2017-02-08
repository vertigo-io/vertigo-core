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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertigo.commons.peg.PegNoMatchFoundException;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.persona.impl.security.dsl.model.DslExpression;
import io.vertigo.persona.impl.security.dsl.model.DslFixedValue;
import io.vertigo.persona.impl.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.impl.security.dsl.model.DslUserPropertyValue;
import io.vertigo.persona.impl.security.dsl.rules.DslParserUtil;
import io.vertigo.util.StringUtil;

/**
 *
 *
 * @author npiedeloup
 */
public final class SqlSecurityRuleTranslator {

	private final List<DslMultiExpression> myMultiExpressions = new ArrayList<>();
	private Map<String, String[]> myUserCriteria;

	/**
	 * Set security pattern.
	 * @param securityRule security Pattern (not null, could be empty)
	 * @return this builder
	 */
	public SqlSecurityRuleTranslator withRule(final String securityRule) {
		Assertion.checkNotNull(securityRule);
		//-----
		try {
			final DslMultiExpression myMultiExpression = DslParserUtil.parseMultiExpression(securityRule);
			myMultiExpressions.add(myMultiExpression);
		} catch (final PegNoMatchFoundException e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getFullMessage());
			throw new WrappedException(message, e);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture de la securityRule {0}\n{1}", securityRule, e.getMessage());
			throw new WrappedException(message, e);
		}
		return this;
	}

	/**
	 * Set criteria.
	 * @param userCriteria Criteria
	 * @return this builder
	 */
	public SqlSecurityRuleTranslator withCriteria(final Map<String, String[]> userCriteria) {
		Assertion.checkNotNull(userCriteria);
		Assertion.checkState(myUserCriteria == null, "criteria was already set : {0}", myUserCriteria);
		//-----
		myUserCriteria = userCriteria;
		return this;

	}

	public String toSql() {
		return buildQueryString();
	}

	private static final String DEFAULT_BOOL_SEP = " OR ";

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		String sep = "";
		for (final DslMultiExpression multiExpressionDefinition : myMultiExpressions) {
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
		final String boolSep = " " + multiExpressionDefinition.getBoolOperator().toString() + " ";
		if (multiExpressionDefinition.isBlock()) {
			query.append("(");
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
			query.append(")");
		}
	}

	private void appendExpression(final StringBuilder query, final DslExpression expressionDefinition) {

		query.append(expressionDefinition.getFieldName());
		if (expressionDefinition.getValue() instanceof DslUserPropertyValue) {
			final DslUserPropertyValue userPropertyValue = (DslUserPropertyValue) expressionDefinition.getValue();
			final String[] userValues = myUserCriteria.get(userPropertyValue.getUserProperty());
			if (userValues != null && userValues.length > 0) {
				if (userValues.length == 1) {
					query
							.append(expressionDefinition.getOperator())
							.append(userValues[0]);
				} else {
					query.append(" IN (");
					String inSep = "";
					for (final String userValue : userValues) {
						query.append(inSep);
						query.append(userValue);
						inSep = ",";
					}
					query.append(")");
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
