package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

/**
 * A  terminal succeeds if the first character of the input string matches that terminal.
 * Sinon retourne une erreur.
 * @author pchretien
 */
public final class TermRule implements Rule<String>, Parser<String> {
	private final String term;

	/**
	 * Constructor.
	 * @param term Terminal
	 */
	public TermRule(final String term) {
		Assertion.checkNotNull(term, "Terminal is mandatory");
		//---------------------------------------------------------------------
		this.term = term;
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return "'" + term + "'";
	}

	@Override
	public Parser<String> createParser() {
		//Parser of terminal is threadsafe.
		return this;
	}

	/** {@inheritDoc} */
	public int parse(final String text, final int start) throws NotFoundException {
		final int end = Math.min(start + term.length(), text.length());
		int match = start;
		//On recherche jusqu'ou le text match avec la règle
		while (match < end && text.charAt(match) == term.charAt(match - start)) {
			match++;
		}
		//Si on est allé au bout de la règle, c'est bon
		if (match == start + term.length()) {
			return match;
		}
		throw new NotFoundException(text, match, null, "Terminal '{0}' is expected", term);
	}

	@Override
	public String get() {
		return term;
	}
}
