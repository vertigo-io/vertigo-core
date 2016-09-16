/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.peg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * The first rule that matches is taken.
 * If no rule is found then an notFoundException is thrown.
 *
 * @author pchretien
 */
final class PegFirstOfRule implements PegRule<PegChoice> {
	private final List<PegRule<?>> rules;
	private final String expression;

	/**
	 * Constructor.
	 * @param rules the list of rules to test
	 */
	PegFirstOfRule(final PegRule<?>... rules) {
		this(Arrays.asList(rules));
	}

	/**
	 * Constructor.
	 * @param rules the list of rules to test
	 */
	PegFirstOfRule(final List<PegRule<?>> rules) {
		Assertion.checkNotNull(rules);
		//-----
		this.rules = Collections.unmodifiableList(rules);
		//---
		final StringBuilder buffer = new StringBuilder();
		for (final PegRule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" | ");
			}
			buffer.append(rule.getExpression());
		}
		expression = buffer.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * @return the list of rules to test
	 */
	private List<PegRule<?>> getRules() {
		return rules;
	}

	/** {@inheritDoc} */
	@Override
	public PegResult<PegChoice> parse(final String text, final int start) throws PegNoMatchFoundException {
		//Règle ayant été le plus profond
		PegNoMatchFoundException best = null;
		int bestIndex = -1;
		for (int i = 0; i < getRules().size(); i++) {
			try {
				final PegResult<?> parserCursor = getRules().get(i)
						.parse(text, start);
				final int end = parserCursor.getIndex();
				if (end < bestIndex) {
					//best est non null, car affecté en même temps que bestIndex
					throw best; //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
				}
				final PegChoice result = new PegChoice(i, parserCursor.getResult());
				return new PegResult<>(end, result);
			} catch (final PegNoMatchFoundException e) {
				//Tant que l'on a des erreurs sur l'évaluation des règles
				//on recommence jusqu'à trouver la première qui fonctionne.
				if (e.getIndex() > bestIndex) {
					bestIndex = e.getIndex();
					best = e;
				}
			}
		}
		//Nothing has been found
		if (best == null) {
			throw new PegNoMatchFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", getExpression());
		}
		throw best;
	}
}
