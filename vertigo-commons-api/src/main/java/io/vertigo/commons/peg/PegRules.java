package io.vertigo.commons.peg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.vertigo.commons.peg.PegRule.Dummy;
import io.vertigo.commons.peg.PegWordRule.Mode;

public final class PegRules {
	private static final Map<String, String> ALL_RULES = Collections.synchronizedMap(new LinkedHashMap<>());
	private static final Map<String, PegNamedRule> NAMED_RULES = Collections.synchronizedMap(new LinkedHashMap<>());

	public static <R> PegRule<R> named(final AbstractRule namedRule, final PegRule<R> innerRule, final String ruleName) {
		final PegNamedRule<R> result = new PegNamedRule<>(innerRule, ruleName);
		ALL_RULES.put(namedRule.getExpression(), "NonTerminal('" + ruleName + "', '#" + ruleName + "')");
		//ALL_RULES.put(result, "NonTerminal('" + ruleName + "', '#" + ruleName + "')");
		NAMED_RULES.put(ruleName, result);
		return populateGramar(result, () -> "NonTerminal('" + ruleName + "', '#" + ruleName + "')");
	}

	public static void wrapped(final AbstractRule<?, ?> abstractRule, final PegRule<?> innerRule) {
		populateGramar(abstractRule, () -> readGramar(innerRule));
	}

	public static <R> PegRule<Optional<R>> optional(final PegRule<R> rule) {
		return populateGramar(new PegOptionalRule(rule), () -> "Optional(" + readGramar(rule) + ")");
	}

	/**
	 * @param term Terminal
	 */
	public static PegRule<String> term(final String term) {
		return populateGramar(new PegTermRule(term), () -> "'" + term + "'");
	}

	public static PegRule<List<?>> sequence(final PegRule<?>... rules) {
		return sequence(Arrays.asList(rules));
	}

	public static PegRule<List<?>> sequence(final List<PegRule<?>> rules) {
		return populateGramar(new PegSequenceRule(rules),
				() -> "Sequence(" +
						(rules.isEmpty() ? "Skip()" : rules.stream()
								.map(rule -> readGramar(rule))
								.collect(Collectors.joining(", ")))
						+ ")");
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
		return populateGramar(new PegChoiceRule(rules),
				() -> "Choice(0," +
						(rules.isEmpty() ? "Skip()" : rules.stream()
								.map(rule -> readGramar(rule))
								.collect(Collectors.joining(", ")))
						+ ")");
	}

	public static <R> PegRule<List<R>> zeroOrMore(final PegRule<R> rule, final boolean repeat) {
		return populateGramar(new PegManyRule<>(rule, true, repeat), () -> "ZeroOrMore(" + readGramar(rule) + ")");
	}

	public static <R> PegRule<List<R>> oneOrMore(final PegRule<R> rule, final boolean repeat) {
		return populateGramar(new PegManyRule<>(rule, false, repeat), () -> "OneOrMore(" + readGramar(rule) + ")");
	}

	public static PegRule<Dummy> skipBlanks(final String blanks) {
		return populateGramar(new PegWhiteSpaceRule(blanks), () -> "' '");
	}

	/**
	 * @param emptyAccepted Si les mots vides sont acceptés
	 * @param checkedChars Liste des caractères vérifiés
	 * @param mode Indique le comportement du parseur : si les caractères vérifiés sont les seuls acceptés, sinon les seuls rejetés, et si l'echappement est autorisé
	 * @param readableExpression Expression nommée
	 */
	public static PegRule<String> word(final boolean emptyAccepted, final String checkedChars, final Mode mode, final String readableExpression) {
		return populateGramar(new PegWordRule(emptyAccepted, checkedChars, mode, readableExpression),
				() -> "NonTerminal('" + readableExpression + "')");
	}

	public static <M> PegRule<M> populateGramar(final PegRule<M> rule, final Supplier<String> expressionHtml) {
		ALL_RULES.put(rule.getExpression(), "Calculating");
		ALL_RULES.put(rule.getExpression(), expressionHtml.get());
		//System.out.println(rule + " -> " + readGramar(rule));
		return rule;
	}

	private static String readGramar(final PegRule<?> rule) {
		final String result = ALL_RULES.get(rule.getExpression());
		if (result == null) {
			System.out.println("Not found : " + rule + " " + rule.getExpression());
		}
		return result;
	}

	public static final String namedRulesAsHtml() {
		final List<PegNamedRule> rules = new ArrayList<>(NAMED_RULES.values());
		Collections.reverse(rules);
		return rules
				.stream()
				.map(rule -> new StringBuilder()
						.append("<h1 id='").append(rule.getRuleName()).append("'>").append(rule.getRuleName()).append("</h1>\n")
						.append("<div>").append(rule.getRule().getExpression()).append("</div>\n")
						.append("<div>").append(readGramar(rule.getRule())).append("</div>\n")
						.append("<script>\n")
						.append("Diagram(\n\t")
						.append(readGramar(rule.getRule()))
						.append("\n).addTo();\n")
						.append("</script>\n\n")
						.toString())
				.collect(Collectors.joining());
	}
}
