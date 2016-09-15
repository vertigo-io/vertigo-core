package io.vertigo.commons.parser;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.parser.Rule.Dummy;
import io.vertigo.commons.parser.WordRule.Mode;

public final class Rules {
	public static <R> Rule<Optional<R>> optional(final Rule<R> rule) {
		return new OptionalRule<>(rule);
	}

	/**
	 * @param term Terminal
	 */
	public static Rule<String> term(final String term) {
		return new TermRule(term);
	}

	/**
	 */
	public static Rule<List<?>> sequence(final Rule<?>... rules) {
		return new SequenceRule(rules);
	}

	/**
	 * @param rules the list of rules to test
	 */
	public static Rule<Choice> firstOf(final Rule<?>... rules) {
		return new FirstOfRule(rules);
	}

	/**
	 * @param rules the list of rules to test
	 */
	public static Rule<Choice> firstOf(final List<Rule<?>> rules) {
		return new FirstOfRule(rules);
	}

	//	public static <R> Rule<List<R>> many(final Rule<R> rule, final boolean emptyAccepted, final boolean repeat) {
	//		return new ManyRule<>(rule, emptyAccepted, repeat);
	//	}

	public static <R> Rule<List<R>> zeroOrMore(final Rule<R> rule, final boolean repeat) {
		return new ManyRule<>(rule, true, repeat);
	}

	public static <R> Rule<List<R>> oneOrMore(final Rule<R> rule, final boolean repeat) {
		return new ManyRule<>(rule, false, repeat);
	}

	public static Rule<Dummy> skipBlanks(final String blanks) {
		return new WhiteSpaceRule(blanks);
	}

	/**
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 */
	public static Rule<String> word(final boolean emptyAccepted, final String checkedChars, final Mode mode) {
		return new WordRule(emptyAccepted, checkedChars, mode);
	}

	/**
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 * @param readableCheckedChar Expression lisible des caractères vérifiés
	 */
	public static Rule<String> word(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableCheckedChar) {
		return new WordRule(emptyAccepted, checkedChars, mode, readableCheckedChar);
	}
}
