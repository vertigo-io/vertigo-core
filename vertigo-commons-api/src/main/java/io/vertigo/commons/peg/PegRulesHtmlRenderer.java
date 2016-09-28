package io.vertigo.commons.peg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

public final class PegRulesHtmlRenderer {

	private final Map<PegRule, String> ALL_RULES = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<String, PegNamedRule> NAMED_RULES = Collections.synchronizedMap(new LinkedHashMap<>());

	private void named(final PegNamedRule namedRule) {
		Assertion.checkState(!NAMED_RULES.containsKey(namedRule.toString()), "{0} already knowned", namedRule.toString());
		NAMED_RULES.put(namedRule.toString(), namedRule);
		populateGramar(namedRule, "NonTerminal('" + namedRule.getRuleName() + "', '#" + namedRule.toString() + "')");
	}

	private void optional(final PegOptionalRule rule) {
		populateGramar(rule, "Optional(" + readGramar(rule.getRule()) + ")");
	}

	private void term(final PegTermRule term) {
		populateGramar(term, term.getExpression());
	}

	private void sequence(final PegSequenceRule rule) {
		populateGramar(rule,
				"Sequence(" +
						(rule.getRules().isEmpty() ? "Skip()" : rule.getRules().stream()
								.map(subRule -> readGramar(subRule))
								.collect(Collectors.joining(", ")))
						+ ")");
	}

	private void choice(final PegChoiceRule rule) {
		populateGramar(rule,
				"Choice(0," +
						(rule.getRules().isEmpty() ? "Skip()" : rule.getRules().stream()
								.map(subRule -> readGramar(subRule))
								.collect(Collectors.joining(", ")))
						+ ")");
	}

	private void many(final PegManyRule<?> rule) {
		populateGramar(rule, (rule.isEmptyAccepted() ? "Zero" : "One") + "OrMore(" + readGramar(rule.getRule()) + ")");
	}

	private void whiteSpace(final PegWhiteSpaceRule rule) {
		populateGramar(rule, "' '");
	}

	private void word(final PegWordRule rule) {
		populateGramar(rule,
				"NonTerminal('" + rule.getExpression() + "')");
	}

	private void populateGramar(final PegRule<?> rule, final String expressionHtml) {
		ALL_RULES.put(rule, expressionHtml);
	}

	private String readGramar(final PegRule<?> rule) {
		final String result = ALL_RULES.get(rule);
		if (result == null) {
			detectGrammar(rule);
			return readGramar(rule);
		}
		return result;
	}

	public String render(final PegRule<?> rootRule) {
		detectGrammar(rootRule);
		final List<PegNamedRule> rules = new ArrayList<>(NAMED_RULES.values());
		return rules
				.stream()
				.map(rule -> new StringBuilder()
						.append("<h1 id='").append(rule.toString()).append("'>").append(rule.getRuleName()).append("</h1>\n")
						//.append("<div>").append(rule.getRule().getExpression()).append("</div>\n")
						//.append("<div>").append(readGramar(rule.getRule())).append("</div>\n")
						.append("<script>\n")
						.append("Diagram(\n\t")
						.append(readGramar(rule.getRule()))
						.append("\n).addTo();\n")
						.append("</script>\n\n")
						.toString())
				.collect(Collectors.joining());
	}

	private void detectGrammar(final PegRule<?> rule) {
		if (rule instanceof PegChoiceRule) {
			choice((PegChoiceRule) rule);
		} else if (rule instanceof PegManyRule) {
			many((PegManyRule) rule);
		} else if (rule instanceof PegNamedRule) {
			named((PegNamedRule) rule);
			detectGrammar(((PegNamedRule) rule).getRule());
		} else if (rule instanceof PegOptionalRule) {
			optional((PegOptionalRule) rule);
		} else if (rule instanceof PegSequenceRule) {
			sequence((PegSequenceRule) rule);
		} else if (rule instanceof PegTermRule) {
			term((PegTermRule) rule);
		} else if (rule instanceof PegWhiteSpaceRule) {
			whiteSpace((PegWhiteSpaceRule) rule);
		} else if (rule instanceof PegWordRule) {
			word((PegWordRule) rule);
		} else if (rule instanceof AbstractRule) {
			detectGrammar(((AbstractRule) rule).getMainRule());
			ALL_RULES.put(rule, readGramar(((AbstractRule) rule).getMainRule()));
		} else {
			ALL_RULES.put(rule, rule.getExpression());
		}
	}
}
