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
 * A  terminal rule succeeds if the first character of the input string matches that terminal.
 * If not an exception is thrown.
 *
 * @author pchretien
 */
public final class TermRule implements Rule<String>, Parser<String> {
	private final String term;

	/**
	 * Constructor.
	 * @param term Terminal
	 */
	public TermRule(final String term) {
		Assertion.checkNotNull(term, "Terminal is required");
		//-----
		this.term = term;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "'" + term + "'";
	}

	@Override
	public Parser<String> createParser() {
		//Parser of terminal is threadsafe.
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public ParserCursor<String> parse(final String text, final int start) throws NotFoundException {
		final int end = Math.min(start + term.length(), text.length());
		int match = start;
		//We look how far the text matches with the rule.
		while (match < end && text.charAt(match) == term.charAt(match - start)) {
			match++;
		}
		//if the rule was fully evaluated then it's ok.
		if (match == start + term.length()) {
			return new ParserCursor<>(match, term);
		}
		throw new NotFoundException(text, match, null, "Terminal '{0}' is expected", term);
	}
}
