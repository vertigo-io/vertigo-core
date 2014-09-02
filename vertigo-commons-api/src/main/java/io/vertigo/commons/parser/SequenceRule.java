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

import io.vertigo.core.lang.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * As wikipedia says  
 * The sequence operator e1 e2 first invokes e1, 
 * and if e1 succeeds, subsequently invokes e2 on the remainder of the input string left unconsumed by e1, 
 * and returns the result. 
 * If either e1 or e2 fails, then the sequence expression e1 e2 fails.
 * 
 * @author pchretien
 */
public final class SequenceRule implements Rule<List<?>> {
	private final List<Rule<?>> rules;
	private final String expression;

	/**
	 * Constructeur.
	 */
	public SequenceRule(final Rule<?>... rules) {
		this(Arrays.asList(rules));
	}

	/**
	 * Constructeur.
	 */
	public SequenceRule(final List<Rule<?>> rules) {
		Assertion.checkNotNull(rules);
		//----------------------------------------------------------------------
		this.rules = Collections.unmodifiableList(rules);
		expression = createExpression(rules);
	}

	/*A sequence of rules/expressions is like that : e1 e2 e3 */
	private static String createExpression(final List<Rule<?>> rules) {
		final StringBuilder buffer = new StringBuilder();
		for (final Rule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" ");
			}
			buffer.append(rule.getExpression());
		}
		return buffer.toString();
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return expression;
	}

	public Parser<List<?>> createParser() {
		return new Parser<List<?>>() {
			private List results;

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				results = new ArrayList<>();
				int index = start;
				try {
					for (final Rule<?> rule : rules) {
						final Parser<?> parser = rule.createParser();
						index = parser.parse(text, index);
						results.add(parser.get());
					}
				} catch (final NotFoundException e) {
					throw new NotFoundException(text, e.getIndex(), e, getExpression());
				}
				return index;
			}

			public List<?> get() {
				return results;
			}
		};
	}
}
