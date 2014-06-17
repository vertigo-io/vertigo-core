package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author pchretien
 */
public final class FirstOfRule implements Rule<Choice> {
	private final List<Rule<?>> rules;
	private final String expression;

	/**
	 * Constructeur.
	 */
	public FirstOfRule(final Rule<?>... rules) {
		this(Arrays.asList(rules));
	}

	/**
	 * Constructeur.
	 */
	public FirstOfRule(final List<Rule<?>> rules) {
		Assertion.checkNotNull(rules);
		//----------------------------------------------------------------------
		this.rules = Collections.unmodifiableList(rules);
		//---
		final StringBuilder buffer = new StringBuilder();
		for (final Rule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" | ");
			}
			buffer.append(rule.getExpression());
		}
		expression = buffer.toString();
	}

	/** {@inheritDoc} */
	public final String getExpression() {
		return expression;
	}

	public Parser<Choice> createParser() {
		return new Parser<Choice>() {
			private Choice result;

			/**
			 * @return numéro de la règle ayant aboutie.
			 */
			public Choice get() {
				return result;
			}

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				//Règle ayant été le plus profond
				NotFoundException best = null;
				int bestIndex = -1;
				for (int i = 0; i < rules.size(); i++) {
					try {
						final Parser<?> parser = rules.get(i).createParser();
						final int end = parser.parse(text, start);
						result = new Choice(i, parser.get());
						if (end < bestIndex) {
							throw best; //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
						}
						return end;
					} catch (final NotFoundException e) {
						//i++;
						if (e.getIndex() > bestIndex) {
							bestIndex = e.getIndex();
							best = e;
						}
						//Tant que l'on a des erreurs sur l'évaluation des règles
						//on recommence jusqu'à trouver la première qui fonctionne.
					}
				}
				//On a rien trouvé.
				if (best == null) {
					throw new NotFoundException(text, start, null, "Echec lors de l''évaluation d''une règle FirstOf : ''{0}''", getExpression());
				}
				throw best;
			}
		};
	}
}
