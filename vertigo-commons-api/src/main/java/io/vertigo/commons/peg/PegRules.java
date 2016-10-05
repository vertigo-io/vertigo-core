package io.vertigo.commons.peg;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.commons.peg.PegWordRule.Mode;

/**
 * Factory of all PeRules.
 * @author pchretien
 */
public final class PegRules {

	private PegRules() {
		//no constructor for factory class
	}

	/**
	 * Named a Rule.
	 * @param innerRule Rule to name
	 * @param ruleName Rule name
	 * @return Named rule
	 */
	public static <R> PegRule<R> named(final PegRule<R> innerRule, final String ruleName) {
		return new PegGrammarRule<>(innerRule, ruleName);
	}

	/**
	 * @param rule Inner rule
	 * @return Optional rule
	 */
	public static <R> PegRule<Optional<R>> optional(final PegRule<R> rule) {
		return new PegOptionalRule<>(rule);
	}

	/**
	 * @param term Terminal
	 */
	public static PegRule<String> term(final String term) {
		return new PegTermRule(term);
	}

	/**
	 * @param rules rules list
	 * @return sequence rule of inner rules
	 */
	public static PegRule<List> sequence(final PegRule... rules) {
		return sequence(Arrays.asList(rules));
	}

	/**
	 * @param rules rules list
	 * @return sequence rule of inner rules
	 */
	public static PegRule<List> sequence(final List<PegRule> rules) {
		return new PegSequenceRule(rules);
	}

	/**
	 * @param rules the list of rules to test
	 * @return choice rule of inner rules
	 */
	public static PegRule<PegChoice> choice(final PegRule<?>... rules) {
		return choice(Arrays.asList(rules));
	}

	/**
	 * @param rules the list of rules to test
	 * @return choice rule of inner rules
	 */
	public static PegRule<PegChoice> choice(final List<PegRule> rules) {
		return new PegChoiceRule(rules);
	}

	/**
	 * @param rule Rule to repeat
	 * @param repeat If text should be parsed entirely
	 * @return zeroOrMore rule
	 */
	public static <R> PegRule<List<R>> zeroOrMore(final PegRule<R> rule, final boolean repeat) {
		return new PegManyRule<>(rule, true, repeat);
	}

	/**
	 * @param rule Rule to repeat
	 * @param repeat If text should be parsed entirely
	 * @return oneOrMore rule
	 */
	public static <R> PegRule<List<R>> oneOrMore(final PegRule<R> rule, final boolean repeat) {
		return new PegManyRule<>(rule, false, repeat);
	}

	/**
	 * @param blanks list of char to skip
	 * @return Rule to match any blank char
	 */
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

	/**
	 * @param rootRule Root rule to start with
	 * @return Html railroad diagram
	 */
	public static final String namedRulesAsHtml(final PegRule rootRule) {
		final PegRulesHtmlRenderer pegRulesHtmlRenderer = new PegRulesHtmlRenderer();
		return pegRulesHtmlRenderer.render(rootRule);
	}
}
