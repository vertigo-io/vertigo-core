package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

/**
 * Règle vérifiant que le texte commence par la chaine précisée.
 * Sinon retourne une erreur.
 * @author pchretien
 * @version $Id: TermRule.java,v 1.3 2013/10/22 12:23:44 pchretien Exp $
 */
public final class TermRule implements Rule<String>, Parser<String> {
	private final String term;

	/**
	 * Constructeur.
	 * @param eval String
	 */
	public TermRule(final String term) {
		Assertion.checkNotNull(term, "Terminal non renseigné !");
		//---------------------------------------------------------------------
		this.term = term;
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return "'" + term + "'";
	}

	@Override
	public Parser<String> createParser() {
		//Dans le cas d'un terminal Le parser est threadsafe.
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
		throw new NotFoundException(text, match, null, "''{0}'' attendu", term);
	}

	@Override
	public String get() {
		return term;
	}
}
