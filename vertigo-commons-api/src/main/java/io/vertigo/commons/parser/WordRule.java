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
import io.vertigo.util.StringUtil;

/**
 * Règle a état permettant de récupérer un mot.
 * En précisant :
 * - Soit les caractères acceptés,
 * - Soit les caractères rejetés.
 *
 * @author pchretien
 */
public final class WordRule implements Rule<String> {
	/** Mode de selection des caractères. */
	public enum Mode {
		/** N'accepte que les caractères passés en paramètre. */
		ACCEPT,
		/** Accepte tout sauf les caractères passés en paramètre. */
		REJECT,
		/** Accepte tout sauf les caractères passés en paramètre.
		 * Avec la possibilité d'echaper un caractère avec le \ */
		REJECT_ESCAPABLE
	}

	private static final char escapeChar = '\\';
	private final String acceptedCharacters;
	private final String rejectedCharacters;
	private final String readableCheckedChar;
	private final boolean emptyAccepted;
	private final Mode mode;

	/**
	 * Constructeur.
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 */
	public WordRule(final boolean emptyAccepted, final String checkedChars, final Mode mode) {
		this(emptyAccepted, checkedChars, mode, "[" + encode(checkedChars) + "]");
	}

	/**
	 * Constructeur.
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 * @param readableCheckedChar Expression lisible des caractères vérifiés
	 */
	public WordRule(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableCheckedChar) {
		super();
		Assertion.checkNotNull(mode);
		Assertion.checkNotNull(checkedChars);
		Assertion.checkArgNotEmpty(readableCheckedChar);
		//-----
		this.emptyAccepted = emptyAccepted;
		this.mode = mode;
		if (mode == Mode.ACCEPT) {
			acceptedCharacters = checkedChars;
			rejectedCharacters = "";
		} else {
			acceptedCharacters = "";
			rejectedCharacters = checkedChars;
		}
		this.readableCheckedChar = readableCheckedChar;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		final StringBuilder expression = new StringBuilder();
		if (!acceptedCharacters.isEmpty()) {
			expression.append(readableCheckedChar);
		} else if (!rejectedCharacters.isEmpty()) {
			if (mode == Mode.REJECT_ESCAPABLE) {
				expression.append("(!")
						.append(readableCheckedChar)
						.append("|\\.)");
			} else {
				expression.append('!');
				expression.append(readableCheckedChar);
			}
		} else if (mode == Mode.REJECT || mode == Mode.REJECT_ESCAPABLE) {//tout
			expression.append('.');
		} else {
			throw new IllegalArgumentException("case not implemented");
		}
		expression.append(emptyAccepted ? "*" : "+");
		return expression.toString();
	}

	/** {@inheritDoc} */
	@Override
	public ParserCursor<String> parse(final String text, final int start) throws NotFoundException {
		int index = start;
		// On vérifie que le caractère est contenu dans les caractères acceptés.
		// On vérifie que le caractère n'est pas contenu dans les caractères rejetés.
		while (index < text.length()
				&& (mode != Mode.ACCEPT || acceptedCharacters.indexOf(text.charAt(index)) >= 0)
				&& (mode == Mode.REJECT_ESCAPABLE && (index > 0) && text.charAt(index - 1) == escapeChar || (rejectedCharacters.indexOf(text.charAt(index)) < 0))) {
			index++;
		}
		if (!emptyAccepted && index == start) {
			throw new NotFoundException(text, start, null, "Mot respectant {0} attendu", getExpression());
		}
		String word = text.substring(start, index);
		if (mode == Mode.REJECT_ESCAPABLE) {
			word = word.replaceAll("\\\\(.)", "$1");
		}
		return new ParserCursor<>(index, word);
	}

	private static String encode(final String chaine) {
		final StringBuilder result = new StringBuilder(chaine);
		StringUtil.replace(result, "\r", "\\r");
		StringUtil.replace(result, "\n", "\\n");
		StringUtil.replace(result, "\t", "\\t");
		StringUtil.replace(result, "[", "\\[");
		StringUtil.replace(result, "]", "\\]");
		StringUtil.replace(result, "''", "\"");
		return result.toString();
	}
}
