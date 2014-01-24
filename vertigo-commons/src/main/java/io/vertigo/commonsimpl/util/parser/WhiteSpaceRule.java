package io.vertigo.commonsimpl.util.parser;

import io.vertigo.kernel.lang.Assertion;

/**
 * Enl�ve les blancs et les commentaires
 * Cette r�gle ne plante jamais -sauf si un blanc obligatoire n'est pas pr�sent-
 * mais permet de faire avancer l'index.
 * @author pchretien
 * @version $Id: WhiteSpaceRule.java,v 1.7 2013/10/23 11:33:20 pchretien Exp $
 */
public final class WhiteSpaceRule implements Rule<Void>, Parser<Void> {
	private final Rule<String> rule;

	/**
	 * Constructeur.
	 * @param blanks Caract�res "blancs" et commentaires.
	 */
	public WhiteSpaceRule(final String blanks) {
		super();
		Assertion.checkNotNull(blanks);
		//----------------------------------------------------------------------
		rule = new WordRule(true, blanks, WordRule.Mode.ACCEPT);
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "blanks";
	}

	public Parser<Void> createParser() {
		return this;
	}

	/** {@inheritDoc} */
	public int parse(final String text, final int start) throws NotFoundException {
		int lastIndex;
		int index = start;
		index = rule.createParser().parse(text, index);

		//Suppression des commentaires  /*xxxxxxxxxxxxxxx*/
		while (text.length() > index + 2 && text.substring(index, index + 2).equals("/*")) {
			//final int startComment = index;
			index += 2;
			lastIndex = index;
			index = text.indexOf("*/", index);
			if (index < 0) {
				//throw new NotFoundException(text, startComment, null, "Fermeture des commentaires */ non trouv�e");
				throw new NotFoundException(text, lastIndex, null, "Fermeture des commentaires */ non trouv�e");
			}
			index += 2;
			//On supprime les blancs
			index = rule.createParser().parse(text, index);
		}
		return index;
	}

	public Void get() {
		return null;
	}

}
