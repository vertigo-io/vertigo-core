package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Règle ET.
 * Toutes les règles ajoutées avant l'évaluation doivent être vérifiées.
 * On remonte une erreur dés qu'une seule des règles n'est pas suivie.
 * L'erreur précise le début du bloc ET et précise la cause. (C'est à dire l clause et qui n'est pas suivie).
 * @author pchretien
 */
public final class ManyRule<R> implements Rule<List<R>> {
	private final Rule<R> rule;
	private final boolean emptyAccepted;

	private final boolean repeat;

	/**
	 * Constructeur.
	 * @param contentRule Possibilité de la boucle
	 * @param emptyAccepted Si liste vide autorisée
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
	 * @param contentRule Possibilité de la boucle
	 * @param emptyAccepted Si liste vide autorisée
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
							//celé signifie que l"index n a pas avancé, on sort
							results.add(parser.get());
						}
					}
				} catch (final NotFoundException e) {
					best = e;
					if (best.getIndex() > index) { //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
						throw best;
					}
				}
				if (!emptyAccepted && results.isEmpty()) {
					throw new NotFoundException(text, start, best, "Aucun élément de la liste trouvé : {0}", getExpression());
				}
				if (repeat && text.length() > index) {
					throw new NotFoundException(text, start, best, "{0} élément(s) trouvé(s), éléments suivants non parsés selon la règle :{1}", results.size(), getExpression());
				}
				return index;
			}

			public List<R> get() {
				return results;
			}
		};
	}
}
