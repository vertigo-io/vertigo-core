package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;

import java.util.List;

public final class DslParserUtil {

	public static List<DslMultiExpressionDefinition> parseMultiExpression(final String buildQuery) throws NotFoundException {
		final Rule<DslMultiExpressionDefinition> expressionsRule = new DslMultiExpressionRule();
		final ManyRule<DslMultiExpressionDefinition> many = new ManyRule<>(expressionsRule, false, true); //repeat true => on veut tout la chaine
		final Parser<List<DslMultiExpressionDefinition>> parser = many.createParser();
		parser.parse(buildQuery, 0);
		return parser.get();
	}

	public static List<DslUserCriteria> parseUserCriteria(String userString) {
		return DslUserCriteriaRule.parse(userString);
	}
}
