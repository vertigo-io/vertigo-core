package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

/**
 * Une règle peut être vue comme 
 * - la création d'une règle principale
 * - la gestion du résultat du parsing
 * 
 * @author pchretien
 * @version $Id: AbstractRule.java,v 1.6 2013/10/22 12:23:44 pchretien Exp $
 */
public abstract class AbstractRule<R, M> implements Rule<R> {
	private Rule<M> mainRule;

	protected final Rule<M> getMainRule() {
		if (mainRule == null) {
			mainRule = createMainRule();
		}
		return mainRule;
	}

	protected abstract Rule<M> createMainRule();

	/** {@inheritDoc} */
	public String getExpression() {
		return getMainRule().getExpression();
	}

	protected abstract R handle(M parsing);

	public final Parser<R> createParser() {
		return new Parser<R>() {
			private R result;

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				final Parser<M> parser = getMainRule().createParser();
				final int end = parser.parse(text, start);
				//---
				result = handle(parser.get());
				//---
				return end;
			}

			public R get() {
				Assertion.checkNotNull(result);
				return result;
			}
		};
	}
}
