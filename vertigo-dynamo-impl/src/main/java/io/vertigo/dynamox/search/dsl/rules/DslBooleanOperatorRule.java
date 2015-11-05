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
import io.vertigo.commons.parser.TermRule;

import java.util.List;

/**
 * Parsing rule for boolean operator.
 * || or OR && and AND
 * @author npiedeloup
 */
final class DslBooleanOperatorRule extends AbstractRule<String, List<?>> {
	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "bool";
	}

	/** {@inheritDoc} */
	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<List<?>> booleanOperatorRule = new SequenceRule(
				DslSyntaxRules.SPACES, //0
				new FirstOfRule(//"single or multiple") //1
						new TermRule("AND"), //0
						new TermRule("and"), //1
						new TermRule("And"), //2
						new TermRule("&&"), //3
						new TermRule("OR"), //4
						new TermRule("Or"), //5
						new TermRule("or"), //6
						new TermRule("||") //7
				),
				DslSyntaxRules.SPACES //2
		);
		return booleanOperatorRule;
	}

	/** {@inheritDoc} */
	@Override
	protected String handle(final List<?> parsing) {
		final String preSpaces = (String) parsing.get(0);
		final String operator = (String) ((Choice) parsing.get(1)).getResult();
		final String postSpaces = (String) parsing.get(2);
		return DslUtil.concat(preSpaces, operator, postSpaces);
	}
}
