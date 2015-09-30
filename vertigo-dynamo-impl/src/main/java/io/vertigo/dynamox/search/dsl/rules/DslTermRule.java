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
import io.vertigo.dynamox.search.dsl.definition.DslTermDefinition;
import io.vertigo.lang.Option;

import java.util.List;

/**
 * Parsing rule for term.
 * #(preTerm)(termField)(postTerm)#!\((defaultValue)\)
 * @author npiedeloup
 */
public final class DslTermRule extends AbstractRule<DslTermDefinition, List<?>> {

	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<List<?>> defaultValueRule = new SequenceRule(
				new TermRule("!("),
				new WordRule(false, ")", WordRule.Mode.REJECT),//1
				new TermRule(")"));

		return new SequenceRule(
				DslSyntaxRules.SPACES,
				DslSyntaxRules.TERM_MARK,
				DslSyntaxRules.PRE_MODIFIER_VALUE,//2
				DslSyntaxRules.WORD, //3
				DslSyntaxRules.POST_MODIFIER_VALUE, //4
				DslSyntaxRules.TERM_MARK,
				new OptionRule<>(defaultValueRule)); //6
	}

	@Override
	protected DslTermDefinition handle(final List<?> parsing) {
		final String preSpaces = (String) parsing.get(0);
		//final String postSpaces = (String) parsing.get(2);

		final String preTerm = (String) parsing.get(2);
		final String termField = (String) parsing.get(3);
		final String postTerm = (String) parsing.get(4);
		final Option<List<?>> defaultRule = (Option<List<?>>) parsing.get(6);
		final Option<String> defaultValue;
		if (defaultRule.isDefined()) {
			defaultValue = Option.option((String) defaultRule.get().get(1));
		} else {
			defaultValue = Option.none();
		}
		return new DslTermDefinition(DslUtil.concat(preSpaces, preTerm), termField, postTerm, defaultValue);
	}

}
