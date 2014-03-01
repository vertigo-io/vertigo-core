package io.vertigo.commons.parser;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.commons.parser.WhiteSpaceRule;
import io.vertigo.commons.parser.WordRule;

import java.util.List;

final class CalculatorRule extends AbstractRule<Integer, List<?>> {
	private static final Rule<Void> SPACES = new WhiteSpaceRule(" ");

	//---Liste des opérations gérées
	private static final Rule<String> ADD = new TermRule("+");
	private static final Rule<String> MINUS = new TermRule("-");
	private static final Rule<String> MULTI = new TermRule("*");
	private static final Rule<String> DIV = new TermRule("/");
	private static final Rule OPERATOR = new FirstOfRule(MULTI, DIV, ADD, MINUS);

	//---Par simplicité un nombre est une suite de chiffres
	private static final Rule<String> NUMBER = new WordRule(false, "0123456789", WordRule.Mode.ACCEPT);

	private static final Rule<List<?>> EXPRESSION = new SequenceRule(//
			NUMBER, //0
			SPACES,//
			OPERATOR,//
			SPACES,//
			NUMBER //4
	);

	@Override
	protected Rule<List<?>> createMainRule() {
		return EXPRESSION;
	}

	@Override
	protected Integer handle(final List<?> parsing) {
		final Integer a = Integer.parseInt((String) parsing.get(0));
		final Integer b = Integer.parseInt((String) parsing.get(4));
		final Choice tuple = (Choice) parsing.get(2);
		switch (tuple.getValue()) {
			case 0:
				return a * b;
			case 1:
				return a / b;
			case 2:
				return a + b;
			case 3:
				return a - b;
			default:
				throw new IllegalStateException();
		}
	}
}
