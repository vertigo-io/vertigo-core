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

import java.util.List;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WordRule;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery;
import io.vertigo.dynamox.search.dsl.model.DslTermQuery.EscapeMode;
import io.vertigo.lang.Option;

/**
 * Parsing rule for query.
 * (preQuery)(term)(postQuery)
 * @author npiedeloup
 */
final class DslTermQueryRule extends AbstractRule<DslTermQuery, List<?>> {
	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "query";
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<Choice> escapeModeRule = new FirstOfRule(
				new TermRule("?(removeReserved)"), //choice 0
				new TermRule("?(escapeReserved)")); //choice 1

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
				new OptionRule<>(escapeModeRule), //5
				new OptionRule<>(defaultValueRule)); //6

		return new SequenceRule(
				DslSyntaxRules.SPACES,//0
				DslSyntaxRules.PRE_MODIFIER_VALUE,//1
				termRule, //2
				DslSyntaxRules.POST_MODIFIER_VALUE); //3);
	}

	/** {@inheritDoc} */
	@Override
	protected DslTermQuery handle(final List<?> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String preQuery = (String) parsing.get(1);

		final List<?> term = (List<?>) parsing.get(2);
		final String preTerm = (String) term.get(1);
		final String termField = (String) term.get(2);
		final String postTerm = (String) term.get(3);
		final Option<Choice> escapeRule = (Option<Choice>) term.get(5);
		final EscapeMode escapeMode;
		if (escapeRule.isPresent()) {
			switch (escapeRule.get().getValue()) {
				case 0:
					escapeMode = EscapeMode.remove;
					break;
				case 1:
					escapeMode = EscapeMode.escape;
					break;
				default:
					throw new IllegalArgumentException("case " + escapeRule.get().getValue() + " not implemented");
			}
		} else {
			escapeMode = EscapeMode.none;
		}
		final Option<List<?>> defaultRule = (Option<List<?>>) term.get(6);
		final Option<String> defaultValue;
		if (defaultRule.isPresent()) {
			defaultValue = Option.ofNullable((String) defaultRule.get().get(1));
		} else {
			defaultValue = Option.empty();
		}

		final String postQuery = (String) parsing.get(3);
		//final String postSpaces = (String) parsing.get(4);
		return new DslTermQuery(DslUtil.concat(preSpaces, preQuery), preTerm, termField, postTerm, escapeMode, defaultValue, postQuery);
	}

}
