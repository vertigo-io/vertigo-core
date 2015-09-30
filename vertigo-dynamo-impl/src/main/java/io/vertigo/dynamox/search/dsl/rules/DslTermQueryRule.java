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
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WordRule;
import io.vertigo.dynamox.search.dsl.definition.DslTermQueryDefinition;
import io.vertigo.lang.Option;

import java.util.List;

/**
 * Parsing rule for query.
 * (preQuery)(term)(postQuery)
 * @author npiedeloup
 */
public final class DslTermQueryRule extends AbstractRule<DslTermQueryDefinition, List<?>> {
	@Override
	public String getExpression() {
		return "query";
	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<List<?>> defaultValueRule = new SequenceRule(
				new TermRule("!("),
				new WordRule(false, ")", WordRule.Mode.REJECT),//1
				new TermRule(")"));

		final Rule<List<?>> termRule = new SequenceRule(
				DslSyntaxRules.TERM_MARK,
				DslSyntaxRules.PRE_MODIFIER_VALUE,//1
				DslSyntaxRules.WORD, //2
				DslSyntaxRules.POST_MODIFIER_VALUE, //3
				DslSyntaxRules.TERM_MARK,
				new OptionRule<>(defaultValueRule)); //5

		return new SequenceRule(
				DslSyntaxRules.SPACES,//0
				DslSyntaxRules.PRE_MODIFIER_VALUE,//1
				termRule, //2
				DslSyntaxRules.POST_MODIFIER_VALUE); //3);
	}

	@Override
	protected DslTermQueryDefinition handle(final List<?> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String preQuery = (String) parsing.get(1);

		final List<?> term = (List<?>) parsing.get(2);
		final String preTerm = (String) term.get(1);
		final String termField = (String) term.get(2);
		final String postTerm = (String) term.get(3);
		final Option<List<?>> defaultRule = (Option<List<?>>) term.get(5);
		final Option<String> defaultValue;
		if (defaultRule.isDefined()) {
			defaultValue = Option.option((String) defaultRule.get().get(1));
		} else {
			defaultValue = Option.none();
		}

		final String postQuery = (String) parsing.get(3);
		//final String postSpaces = (String) parsing.get(4);
		return new DslTermQueryDefinition(DslUtil.concat(preSpaces, preQuery), preTerm, termField, postTerm, defaultValue, postQuery);
	}

}
