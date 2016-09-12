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
package io.vertigo.commons.parser;

import io.vertigo.lang.Assertion;

/**
 * This class parses a text and tries all the rules until a rule matches.
 *
 * @author pchretien
 */
final class FirstOfRuleParser implements Parser<Choice> {
	private final FirstOfRule firstOfRule;

	FirstOfRuleParser(final FirstOfRule firstOfRule) {
		Assertion.checkNotNull(firstOfRule);
		//-----
		this.firstOfRule = firstOfRule;
	}

	/** {@inheritDoc} */
	@Override
	public ParserCursor<Choice> parse(final String text, final int start) throws NotFoundException {
		//Règle ayant été le plus profond
		NotFoundException best = null;
		int bestIndex = -1;
		for (int i = 0; i < firstOfRule.getRules().size(); i++) {
			try {
				final Parser<?> parser = firstOfRule.getRules().get(i).createParser();
				final ParserCursor<?> parserCursor = parser.parse(text, start);
				final int end = parserCursor.getIndex();
				final Choice result = new Choice(i, parserCursor.getResult());
				if (end < bestIndex) {
					//best est non null, car affecté en même temps que bestIndex
					throw best; //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
				}
				return new ParserCursor(end, result);
			} catch (final NotFoundException e) {
				if (e.getIndex() > bestIndex) {
					bestIndex = e.getIndex();
					best = e;
				}
				//Tant que l'on a des erreurs sur l'évaluation des règles
				//on recommence jusqu'à trouver la première qui fonctionne.
			}
		}
		//Nothing has been found
		if (best == null) {
			throw new NotFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", firstOfRule.getExpression());
		}
		throw best;
	}
}
