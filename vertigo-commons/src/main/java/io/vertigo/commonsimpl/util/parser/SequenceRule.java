package io.vertigo.commonsimpl.util.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author pchretien
 * @version $Id: SequenceRule.java,v 1.6 2013/10/22 12:23:44 pchretien Exp $
 */
public final class SequenceRule implements Rule<List<?>> {
	private final List<Rule<?>> rules;
	private final String expression;

	/**
	 * Constructeur.
	 */
	public SequenceRule(final Rule<?>... rules) {
		this(Arrays.asList(rules));
	}

	/**
	 * Constructeur.
	 */
	public SequenceRule(final List<Rule<?>> rules) {
		Assertion.checkNotNull(rules);
		//----------------------------------------------------------------------
		this.rules = Collections.unmodifiableList(rules);

		final StringBuilder buffer = new StringBuilder();
		for (final Rule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" ");
			}
			buffer.append(rule.getExpression());
		}
		expression = buffer.toString();
	}

	/** {@inheritDoc} */
	public String getExpression() {
		return expression;
	}

	public Parser<List<?>> createParser() {
		return new Parser<List<?>>() {
			private List results;

			/** {@inheritDoc} */
			public int parse(final String text, final int start) throws NotFoundException {
				results = new ArrayList<>();
				int index = start;
				try {
					for (final Rule<?> rule : rules) {
						final Parser<?> parser = rule.createParser();
						index = parser.parse(text, index);
						results.add(parser.get());
					}
				} catch (final NotFoundException e) {
					throw new NotFoundException(text, e.getIndex(), e, getExpression());
				}
				return index;
			}

			public List<?> get() {
				return results;
			}
		};
	}
}
