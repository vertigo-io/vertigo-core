package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;


/**
 * R�gle ET.
 * Toutes les r�gles ajout�es avant l'�valuation doivent �tre v�rifi�es.
 * On remonte une erreur d�s qu'une seule des r�gles n'est pas suivie.
 * L'erreur dpr�cise le d�but du bloc ET et pr�cise la cause. (C'est � dire l clause et qui n'est pas suivie).
 * @author pchretien
 * @version $Id: ManyRule.java,v 1.5 2013/10/22 12:23:44 pchretien Exp $
 */
public final class ManyRule<R> implements Rule<List<R>> {
	private final Rule<R> rule;
	private final boolean emptyAccepted;

	private final boolean repeat;

	/**
	 * Constructeur.
	 * @param contentRule Possibilit� de la boucle
	 * @param emptyAccepted Si liste vide autoris�e
	 */
	public ManyRule(final Rule<R> rule, final boolean emptyAccepted, final boolean repeat) {
		Assertion.checkNotNull(rule);
		//---------------------------------------------------------------------
		this.rule = rule;
		this.emptyAccepted = emptyAccepted;
		this.repeat = repeat;
	}

	/**
	 * Constructeur.
	 * @param contentRule Possibilit� de la boucle
	 * @param emptyAccepted Si liste vide autoris�e
	 */
	public ManyRule(final Rule<R> rule, final boolean emptyAccepted) {
		this(rule, emptyAccepted, false);
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return "(" + rule.getExpression() + ")" + (emptyAccepted ? "*" : "+");
	}

	public Parser<List<R>> createParser() {
		return new Parser<List<R>>() {
			private List<R> results;

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				int index = start;
				//======================================================================
				results = new ArrayList<>();
				NotFoundException best = null;
				try {
					int prevIndex = -1;
					while (index < text.length() && index > prevIndex) {
						prevIndex = index;
						final Parser<R> parser = rule.createParser();
						index = parser.parse(text, index);
						if (index > prevIndex) {
							//cel� signifie que l"index n a pas avanc�, on sort
							results.add(parser.get());
						}
					}
				} catch (final NotFoundException e) {
					best = e;
					if (best.getIndex() > index) { //Si on a plus avanc� avec une autre r�gle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
						throw best;
					}
				}
				if (!emptyAccepted && results.isEmpty()) {
					throw new NotFoundException(text, start, best, "Aucun �l�ment de la liste trouv� : {0}", getExpression());
				}
				if (repeat && text.length() > index) {
					throw new NotFoundException(text, start, best, "{0} �l�ment(s) trouv�(s), �l�ments suivants non pars�s selon la r�gle :{1}", results.size(), getExpression());
				}
				return index;
			}

			public List<R> get() {
				return results;
			}
		};
	}
}
