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
import io.vertigo.dynamox.search.dsl.definition.DslBlockQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslQueryDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsing rule for query.
 * (preMultiQuery)\((queries|rangeQuery|fixedQuery|multiQuery|)+\)(postMultiQuery)
 * @author npiedeloup
 */
final class DslMultiQueryRule extends AbstractRule<DslBlockQueryDefinition, List<?>> {
	private final static int MAX_DEPTH = 3;
	private final int level;

	/**
	 * Constructor.
	 */
	DslMultiQueryRule() {
		level = 0;
	}

	private DslMultiQueryRule(final int level) {
		this.level = level;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "multiQuery";
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {
		if (level > MAX_DEPTH) {
			return (Rule<List<?>>) DslSyntaxRules.DEPTH_OVERFLOW;
		}

		final Rule<Choice> queriesRule = new FirstOfRule(//"single or multiple")
				new DslTermQueryRule(), //0
				new DslRangeQueryRule(), //1
				new DslMultiQueryRule(level + 1), //2
				new DslFixedQueryRule() //3
		);

		final Rule<List<Choice>> manyQueriesRule = new ManyRule<>(queriesRule, false);
		return new SequenceRule(
				DslSyntaxRules.PRE_MODIFIER_VALUE,//0
				DslSyntaxRules.BLOCK_START,
				manyQueriesRule, //2
				DslSyntaxRules.BLOCK_END,
				DslSyntaxRules.POST_MODIFIER_VALUE); //4
	}

	/** {@inheritDoc} */
	@Override
	protected DslBlockQueryDefinition handle(final List<?> parsing) {
		final String preQuery = (String) parsing.get(0);
		final List<DslQueryDefinition> queryDefinitions = new ArrayList<>();
		final String postQuery = (String) parsing.get(4);
		final List<Choice> manyQueries = (List<Choice>) parsing.get(2);
		for (final Choice item : manyQueries) {
			queryDefinitions.add((DslQueryDefinition) item.getResult());
		}
		return new DslBlockQueryDefinition(preQuery, queryDefinitions, postQuery);
	}
}
