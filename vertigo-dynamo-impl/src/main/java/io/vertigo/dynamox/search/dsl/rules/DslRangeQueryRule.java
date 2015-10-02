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
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.WordRule;
import io.vertigo.dynamox.search.dsl.definition.DslQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslRangeQueryDefinition;

import java.util.List;

/**
 * Parsing rule for query.
 * (preRangeQuery)\[(termQuery|fixedQuery) to (termQuery|fixedQuery)\](postRangeQuery)
 * @author npiedeloup
 */
final class DslRangeQueryRule extends AbstractRule<DslRangeQueryDefinition, List<?>> {

	@Override
	public String getExpression() {
		return "rangeQuery";
	}

	@Override
	protected Rule<List<?>> createMainRule() {

		final Rule<Choice> queriesRule = new FirstOfRule(//"term or fixed")
				new DslTermQueryRule(), //0
				new DslFixedQueryRule() //1
		);

		return new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE,//0
				DslSyntaxRules.ARRAY_START,
				queriesRule, //2
				DslSyntaxRules.SPACES,
				new WordRule(false, "TOto", WordRule.Mode.ACCEPT, "to"),
				DslSyntaxRules.SPACES,
				queriesRule,//6
				DslSyntaxRules.SPACES,
				DslSyntaxRules.ARRAY_END,
				DslSyntaxRules.POST_MODIFIER_VALUE); //9
	}

	@Override
	protected DslRangeQueryDefinition handle(final List<?> parsing) {
		final String preQuery = (String) parsing.get(0);

		final Choice startTermQuery = (Choice) parsing.get(2);
		final DslQueryDefinition startQueryDefinitions = (DslQueryDefinition) startTermQuery.getResult();

		final Choice endTermQuery = (Choice) parsing.get(6);
		final DslQueryDefinition endQueryDefinitions = (DslQueryDefinition) endTermQuery.getResult();

		final String postQuery = (String) parsing.get(9);

		return new DslRangeQueryDefinition(preQuery, startQueryDefinitions, endQueryDefinitions, postQuery);
	}
}
