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
package io.vertigo.commons.parser;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WhiteSpaceRule;
import io.vertigo.commons.parser.WordRule;

import java.util.List;

final class CalculatorRule extends AbstractRule<Integer, List<?>> {
	private static final Rule<Void> SPACES = new WhiteSpaceRule(" ");

	//---Liste des opérations gérées
	private static final Rule<String> ADD = new TermRule("+");
	private static final Rule<String> MINUS = new TermRule("-");
	private static final Rule<String> MULTI = new TermRule("*");
	private static final Rule<String> DIV = new TermRule("/");
	private static final Rule OPERATOR = new FirstOfRule(MULTI, DIV, ADD, MINUS);

	//---Par simplicité un nombre est une suite de chiffres
	private static final Rule<String> NUMBER = new WordRule(false, "0123456789", WordRule.Mode.ACCEPT);

	private static final Rule<List<?>> EXPRESSION = new SequenceRule(//
			NUMBER, //0
			SPACES,//
			OPERATOR,//
			SPACES,//
			NUMBER //4
	);

	@Override
	protected Rule<List<?>> createMainRule() {
		return EXPRESSION;
	}

	@Override
	protected Integer handle(final List<?> parsing) {
		final Integer a = Integer.parseInt((String) parsing.get(0));
		final Integer b = Integer.parseInt((String) parsing.get(4));
		final Choice tuple = (Choice) parsing.get(2);
		switch (tuple.getValue()) {
			case 0:
				return a * b;
			case 1:
				return a / b;
			case 2:
				return a + b;
			case 3:
				return a - b;
			default:
				throw new IllegalStateException();
		}
	}
}
