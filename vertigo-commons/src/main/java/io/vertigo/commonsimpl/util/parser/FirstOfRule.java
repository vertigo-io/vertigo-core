package io.vertigo.commonsimpl.util.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author pchretien
 * @version $Id: FirstOfRule.java,v 1.6 2013/10/22 12:23:44 pchretien Exp $
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
			 * @return num�ro de la r�gle ayant aboutie.
			 */
			public Choice get() {
				return result;
			}

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				//R�gle ayant �t� le plus profond
				NotFoundException best = null;
				int bestIndex = -1;
				for (int i = 0; i < rules.size(); i++) {
					try {
						final Parser<?> parser = rules.get(i).createParser();
						final int end = parser.parse(text, start);
						result = new Choice(i, parser.get());
						if (end < bestIndex) {
							throw best; //Si on a plus avanc� avec une autre r�gle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
						}
						return end;
					} catch (final NotFoundException e) {
						//i++;
						if (e.getIndex() > bestIndex) {
							bestIndex = e.getIndex();
							best = e;
						}
						//Tant que l'on a des erreurs sur l'�valuation des r�gles
						//on recommence jusqu'� trouver la premi�re qui fonctionne.
					}
				}
				//On a rien trouv�.
				if (best == null) {
					throw new NotFoundException(text, start, null, "Echec lors de l''�valuation d''une r�gle FirstOf : ''{0}''", getExpression());
				}
				throw best;
			}
		};
	}
}
