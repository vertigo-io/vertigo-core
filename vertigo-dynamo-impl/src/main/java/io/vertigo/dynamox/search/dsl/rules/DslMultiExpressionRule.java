/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.definition.DslExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsing rule for query.
 * (preMultiExpression)\((expression|multiExpression)+\)(postMultiExpression)
 * @author npiedeloup
 */
public final class DslMultiExpressionRule extends AbstractRule<DslMultiExpressionDefinition, Choice> {
	private final static int MAX_DEPTH = 3;
	private final int level;

	public DslMultiExpressionRule(final int level) {
		this.level = level;
	}

	@Override
	protected Rule<Choice> createMainRule() {
		if (level > MAX_DEPTH) {
			return (Rule<Choice>) DslSyntaxRules.DEPTH_OVERFLOW;
		}
		final Rule<Choice> expressionsRule = new FirstOfRule(//"single or multiple")
				new DslExpressionRule(), //0
				new DslMultiExpressionRule(level + 1) //1
		);
		final Rule<List<Choice>> manyExpressionRule = new ManyRule<>(expressionsRule, false);
		final Rule<List<?>> blockExpressionRule = new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE,//0
				DslSyntaxRules.BLOCK_START, //1
				manyExpressionRule, //2
				DslSyntaxRules.BLOCK_END, //3
				DslSyntaxRules.POST_MODIFIER_VALUE); //4
		final Rule<Choice> blockRule = new FirstOfRule(//"single or multiple")
				blockExpressionRule, //0
				manyExpressionRule //1
		);
		return blockRule;
	}

	@Override
	protected DslMultiExpressionDefinition handle(final Choice parsing) {
		final String preMultiExpression;
		final List<DslExpressionDefinition> expressionDefinitions = new ArrayList<>();
		final List<DslMultiExpressionDefinition> multiExpressionDefinitions = new ArrayList<>();
		final String postMultiExpression;
		//---
		final boolean block = parsing.getValue() == 0;
		final List<Choice> many;
		switch (parsing.getValue()) {
			case 0:
				final List<?> blockExpression = (List<?>) parsing.getResult();
				preMultiExpression = (String) blockExpression.get(0);
				many = (List<Choice>) blockExpression.get(2);
				postMultiExpression = (String) blockExpression.get(4);
				break;
			case 1:
				preMultiExpression = "";
				many = (List<Choice>) parsing.getResult();
				postMultiExpression = "";
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getValue() + " not implemented");
		}
		//On récupère le produit de la règle many
		for (final Choice item : many) {
			switch (item.getValue()) {
				case 0:
					expressionDefinitions.add((DslExpressionDefinition) item.getResult());
					break;
				case 1:
					multiExpressionDefinitions.add((DslMultiExpressionDefinition) item.getResult());
					break;
				default:
					throw new IllegalArgumentException("case " + item.getValue() + " not implemented");
			}
		}
		//---
		return new DslMultiExpressionDefinition(preMultiExpression, block, expressionDefinitions, multiExpressionDefinitions, postMultiExpression);
	}
}
