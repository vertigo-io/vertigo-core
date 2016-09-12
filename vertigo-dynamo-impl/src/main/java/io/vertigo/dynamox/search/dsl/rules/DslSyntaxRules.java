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
package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.ParserCursor;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WordRule;

/**
 *
 * Les lettres interdites dans les mots sont les suivantes espace
 * =();[]"/.
 *
 * @author npiedeloup
 */
final class DslSyntaxRules {
	/** Liste des caractères réservés. */
	private static final String RESERVED = "()[]\"!#$%&'*+,-./:;<=>?@\\^`|~";
	/** Liste des caractères blancs. */
	private static final String WHITE_SPACE = " \t\n\r";

	private static final String PRE_MODIFIER = "~+-*?\"";
	private static final String POST_MODIFIER = "~+-*?^0123456789\"";

	/** Liste des délimiteurs. */
	private static final String DELIMITERS = RESERVED + WHITE_SPACE;

	/** règle de lectures des blancs. */
	static final Rule<?> SPACES = new WordRule(true, WHITE_SPACE, WordRule.Mode.ACCEPT, "_");

	/** array start. */
	static final Rule<String> ARRAY_START = new TermRule("["); //like arrays in json syntax
	/** array end. */
	static final Rule<String> ARRAY_END = new TermRule("]");
	/** array separator. */
	static final Rule<String> ARRAY_SEPARATOR = new TermRule(",");

	/** block start. */
	static final Rule<String> BLOCK_START = new TermRule("(");
	/** block end. */
	static final Rule<String> BLOCK_END = new TermRule(")");

	/** term mark. */
	static final Rule<String> TERM_MARK = new TermRule("#");
	/** field end. */
	static final Rule<String> FIELD_END = new TermRule(":");

	/** premodifier. */
	static final Rule<String> PRE_MODIFIER_VALUE = new WordRule(true, PRE_MODIFIER + WHITE_SPACE, WordRule.Mode.ACCEPT, "PREM");
	/** postmodifier. */
	static final Rule<String> POST_MODIFIER_VALUE = new WordRule(true, POST_MODIFIER, WordRule.Mode.ACCEPT, "POSTM");

	//Il faut gérer le caractère d'évitement.
	/** word. */
	static final Rule<String> WORD = new WordRule(false, DELIMITERS, WordRule.Mode.REJECT, "DEL");

	/** fixed word. */
	static final Rule<String> FIXED_WORD = new WordRule(false, WHITE_SPACE + "]),", WordRule.Mode.REJECT, "!_");

	/** depth overflow. */
	static final Rule<?> DEPTH_OVERFLOW = new Rule<Void>() {

		@Override
		public String getExpression() {
			return "<depth overflow>";
		}

		@Override
		public ParserCursor<Void> parse(final String text, final int start) throws NotFoundException {
			throw new NotFoundException(text, start, null, "Too deep", getExpression());
		}

	};

	private DslSyntaxRules() {
		//Classe sans état
	}

}
