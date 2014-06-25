package io.vertigo.commons.parser;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * As wikipedia says  
 * The sequence operator e1 e2 first invokes e1, 
 * and if e1 succeeds, subsequently invokes e2 on the remainder of the input string left unconsumed by e1, 
 * and returns the result. 
 * If either e1 or e2 fails, then the sequence expression e1 e2 fails.
 * 
 * @author pchretien
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
		expression = createExpression(rules);
	}

	/*A sequence of rules/expressions is like that : e1 e2 e3 */
	private static String createExpression(final List<Rule<?>> rules) {
		final StringBuilder buffer = new StringBuilder();
		for (final Rule<?> rule : rules) {
			if (buffer.length() > 0) {
				buffer.append(" ");
			}
			buffer.append(rule.getExpression());
		}
		return buffer.toString();
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
