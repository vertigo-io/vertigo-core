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
package io.vertigo.dynamox.search.dsl.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.Rules;
import io.vertigo.dynamox.search.dsl.model.DslExpression;
import io.vertigo.dynamox.search.dsl.model.DslMultiExpression;

/**
 * Parsing rule for query.
 * (preMultiExpression)\((expression|multiExpression)+\)(postMultiExpression)
 * @author npiedeloup
 */
final class DslMultiExpressionRule extends AbstractRule<DslMultiExpression, Choice> {
	private static final int MAX_DEPTH = 3;
	private final int level;

	/**
	 * Constructor.
	 */
	DslMultiExpressionRule() {
		this(0);
		//At the beginning the level is always 0
	}

	private DslMultiExpressionRule(final int level) {
		this.level = level;
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<Choice> createMainRule() {
		if (level > MAX_DEPTH) {
			return (Rule<Choice>) DslSyntaxRules.DEPTH_OVERFLOW;
		}
		final Rule<Choice> expressionsRule = Rules.firstOf(//"single or multiple")
				new DslExpressionRule(), //0
				new DslMultiExpressionRule(level + 1) //1
		);
		final Rule<List<Choice>> manyExpressionRule = Rules.oneOrMore(expressionsRule, false);
		final Rule<List<?>> blockExpressionRule = Rules.sequence(
				Rules.optional(new DslBooleanOperatorRule()), //0
				DslSyntaxRules.PRE_MODIFIER_VALUE, //1
				DslSyntaxRules.BLOCK_START, //2
				manyExpressionRule, //3
				DslSyntaxRules.BLOCK_END, //4
				DslSyntaxRules.POST_MODIFIER_VALUE); //5
		return Rules.firstOf(//"single or multiple")
				blockExpressionRule, //0
				manyExpressionRule //1
		);
	}

	/** {@inheritDoc} */
	@Override
	protected DslMultiExpression handle(final Choice parsing) {
		final String preMultiExpression;
		final String postMultiExpression;
		//---
		final boolean block = parsing.getValue() == 0;
		final List<Choice> many;
		switch (parsing.getValue()) {
			case 0:
				final List<?> blockExpression = (List<?>) parsing.getResult();
				preMultiExpression = ((Optional<String>) blockExpression.get(0)).orElse("") + (String) blockExpression.get(1);
				many = (List<Choice>) blockExpression.get(3);
				postMultiExpression = (String) blockExpression.get(5);
				break;
			case 1:
				preMultiExpression = "";
				many = (List<Choice>) parsing.getResult();
				postMultiExpression = "";
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getValue() + " not implemented");
		}

		final List<DslExpression> expressionDefinitions = new ArrayList<>();
		final List<DslMultiExpression> multiExpressionDefinitions = new ArrayList<>();

		//On récupère le produit de la règle many
		for (final Choice item : many) {
			switch (item.getValue()) {
				case 0:
					expressionDefinitions.add((DslExpression) item.getResult());
					break;
				case 1:
					multiExpressionDefinitions.add((DslMultiExpression) item.getResult());
					break;
				default:
					throw new IllegalArgumentException("case " + item.getValue() + " not implemented");
			}
		}
		//---
		return new DslMultiExpression(preMultiExpression, block, expressionDefinitions, multiExpressionDefinitions, postMultiExpression);
	}
}
