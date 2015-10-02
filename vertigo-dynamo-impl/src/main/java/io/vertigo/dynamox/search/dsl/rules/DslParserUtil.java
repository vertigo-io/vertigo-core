package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;

import java.util.List;

/**
 * Util for parsing search patterns and queries.
 * @author npiedeloup
 */
public final class DslParserUtil {

	private DslParserUtil() {
		//nothing
	}

	/**
	 * @param buildQuery Builder pattern
	 * @return Parsed pattern
	 * @throws NotFoundException If pattern doesn't match grammar
	 */
	public static List<DslMultiExpressionDefinition> parseMultiExpression(final String buildQuery) throws NotFoundException {
		final Rule<DslMultiExpressionDefinition> expressionsRule = new DslMultiExpressionRule();
		final ManyRule<DslMultiExpressionDefinition> many = new ManyRule<>(expressionsRule, false, true); //repeat true => on veut tout la chaine
		final Parser<List<DslMultiExpressionDefinition>> parser = many.createParser();
		parser.parse(buildQuery, 0);
		return parser.get();
	}

	/**
	 * @param userString User criteria
	 * @return Parsed User criteria
	 */
	public static List<DslUserCriteria> parseUserCriteria(final String userString) {
		return DslUserCriteriaRule.parse(userString);
	}
}
