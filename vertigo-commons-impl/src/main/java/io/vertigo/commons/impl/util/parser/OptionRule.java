package io.vertigo.commons.impl.util.parser;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

/**
 * R�gle optionnelle.
 * @author pchretien
 * @version $Id: OptionRule.java,v 1.6 2013/10/22 12:23:44 pchretien Exp $
 */
public final class OptionRule<P> implements Rule<Option<P>> {
	private final String expression;
	private final Rule<P> rule;

	/**
	 * Constructeur.
	 * @param rule R�gle optionnelle
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
					//Comme la r�gle est optionnelle si on ne trouve rien on reste au point de d�part. 
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
