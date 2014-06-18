package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

/**
 * Règle optionnelle.
 * @author pchretien
 */
public final class OptionRule<P> implements Rule<Option<P>> {
	private final String expression;
	private final Rule<P> rule;

	/**
	 * Constructeur.
	 * @param rule Règle optionnelle
	 */
	public OptionRule(final Rule<P> rule) {
		super();
		Assertion.checkNotNull(rule);
		//---------------------------------------------------------------------
		expression = rule.getExpression() + "?";
		this.rule = rule;
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return expression;
	}

	public Parser<Option<P>> createParser() {
		return new Parser<Option<P>>() {
			private Option<P> option;

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				int index = start;
				//======================================================================
				option = Option.none();
				try {
					final Parser<P> parser = rule.createParser();
					index = parser.parse(text, index);
					option = Option.option(parser.get());
				} catch (final NotFoundException e) {
					//Comme la règle est optionnelle si on ne trouve rien on reste au point de départ. 
				}
				return index;
			}

			@Override
			public Option<P> get() {
				return option;
			}
		};
	}
}
