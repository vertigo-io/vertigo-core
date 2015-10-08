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

import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;

import java.util.List;

/**
 * Util for parsing search patterns and queries.
 * @author npiedeloup
 */
public final class DslParserUtil {

	private DslParserUtil() {
		//nothing
	}

	/**
	 * @param buildQuery Builder pattern
	 * @return Parsed pattern
	 * @throws NotFoundException If pattern doesn't match grammar
	 */
	public static List<DslMultiExpressionDefinition> parseMultiExpression(final String buildQuery) throws NotFoundException {
		final Rule<DslMultiExpressionDefinition> expressionsRule = new DslMultiExpressionRule();
		final ManyRule<DslMultiExpressionDefinition> many = new ManyRule<>(expressionsRule, false, true); //repeat true => on veut tout la chaine
		final Parser<List<DslMultiExpressionDefinition>> parser = many.createParser();
		parser.parse(buildQuery, 0);
		return parser.get();
	}

	/**
	 * @param userString User criteria
	 * @return Parsed User criteria
	 */
	public static List<DslUserCriteria> parseUserCriteria(final String userString) {
		return DslUserCriteriaRule.parse(userString);
	}
}
