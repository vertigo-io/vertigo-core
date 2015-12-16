package io.vertigo.commons.parser;

import io.vertigo.lang.Assertion;

/**
 * This class parses a text and tries all the rules until a rule matches.
 *   
 * @author pchretien
 */
final class FirstOfRuleParser implements Parser<Choice> {
	private Choice result;
	private final FirstOfRule firstOfRule;

	FirstOfRuleParser(final FirstOfRule firstOfRule) {
		Assertion.checkNotNull(firstOfRule);
		//-----
		this.firstOfRule = firstOfRule;
	}

	/**
	 * @return the choice number that succeeded.
	 */
	@Override
	public Choice get() {
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public int parse(final String text, final int start) throws NotFoundException {
		//Règle ayant été le plus profond
		NotFoundException best = null;
		int bestIndex = -1;
		for (int i = 0; i < firstOfRule.getRules().size(); i++) {
			try {
				final Parser<?> parser = firstOfRule.getRules().get(i).createParser();
				final int end = parser.parse(text, start);
				result = new Choice(i, parser.get());
				if (end < bestIndex) {
					throw best; //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
				}
				return end;
			} catch (final NotFoundException e) {
				if (e.getIndex() > bestIndex) {
					bestIndex = e.getIndex();
					best = e;
				}
				//Tant que l'on a des erreurs sur l'évaluation des règles
				//on recommence jusqu'à trouver la première qui fonctionne.
			}
		}
		//Nothing has been found
		if (best == null) {
			throw new NotFoundException(text, start, null, "No rule found when evalutating  FirstOf : '{0}'", firstOfRule.getExpression());
		}
		throw best;
	}
}
