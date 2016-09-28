package io.vertigo.commons.peg;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.commons.peg.PegWordRule.Mode;

public final class PegRules {

	public static <R> PegRule<R> named(final AbstractRule namedRule, final PegRule<R> innerRule, final String ruleName) {
		return new PegNamedRule<>(innerRule, ruleName);
	}

	public static <R> PegRule<Optional<R>> optional(final PegRule<R> rule) {
		return new PegOptionalRule(rule);
	}

	/**
	 * @param term Terminal
	 */
	public static PegRule<String> term(final String term) {
		return new PegTermRule(term);
	}

	public static PegRule<List<?>> sequence(final PegRule<?>... rules) {
		return sequence(Arrays.asList(rules));
	}

	public static PegRule<List<?>> sequence(final List<PegRule<?>> rules) {
		return new PegSequenceRule(rules);
	}

	/**
	 * @param rules the list of rules to test
	 */
	public static PegRule<PegChoice> choice(final PegRule<?>... rules) {
		return choice(Arrays.asList(rules));
	}

	/**
	 * @param rules the list of rules to test
	 */
	public static PegRule<PegChoice> choice(final List<PegRule<?>> rules) {
		return new PegChoiceRule(rules);
	}

	public static <R> PegRule<List<R>> zeroOrMore(final PegRule<R> rule, final boolean repeat) {
		return new PegManyRule<>(rule, true, repeat);
	}

	public static <R> PegRule<List<R>> oneOrMore(final PegRule<R> rule, final boolean repeat) {
		return new PegManyRule<>(rule, false, repeat);
	}

	public static PegRule<Dummy> skipBlanks(final String blanks) {
		return new PegWhiteSpaceRule(blanks);
	}

	/**
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 * @param readableExpression Expression nommée
	 */
	public static PegRule<String> word(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableExpression) {
		return new PegWordRule(emptyAccepted, checkedChars, mode, readableExpression);
	}

	public static final String namedRulesAsHtml(final PegRule rootRule) {
		final PegRulesHtmlRenderer pegRulesHtmlRenderer = new PegRulesHtmlRenderer();
		return pegRulesHtmlRenderer.render(rootRule);
	}
}
