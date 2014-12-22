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

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Règle optionnelle.
 * @author pchretien
 */
public final class OptionRule<P> implements Rule<Option<P>> {
	private final String expression;
	private final Rule<P> rule;

	/**
	 * Constructeur.
	 * @param rule Règle optionnelle
	 */
	public OptionRule(final Rule<P> rule) {
		super();
		Assertion.checkNotNull(rule);
		//-----
		expression = rule.getExpression() + "?";
		this.rule = rule;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public Parser<Option<P>> createParser() {
		return new Parser<Option<P>>() {
			private Option<P> option;

			/** {@inheritDoc} */
			@Override
			public int parse(final String text, final int start) throws NotFoundException {
				int index = start;
				//======================================================================
				option = Option.none();
				try {
					final Parser<P> parser = rule.createParser();
					index = parser.parse(text, index);
					option = Option.option(parser.get());
				} catch (final NotFoundException e) {
					//Comme la règle est optionnelle si on ne trouve rien on reste au point de départ.
				}
				return index;
			}

			@Override
			public Option<P> get() {
				return option;
			}
		};
	}
}
