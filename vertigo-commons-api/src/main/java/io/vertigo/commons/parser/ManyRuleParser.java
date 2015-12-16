package io.vertigo.commons.parser;

import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pchretien
 */
final class ManyRuleParser<R> implements Parser<List<R>> {
	private final ManyRule<R> manyRule;
	private List<R> results;

	ManyRuleParser(final ManyRule<R> manyRule) {
		Assertion.checkNotNull(manyRule);
		//-----
		this.manyRule = manyRule;
	}

	/** {@inheritDoc} */
	@Override
	public int parse(final String text, final int start) throws NotFoundException {
		int index = start;
		//-----
		results = new ArrayList<>();
		NotFoundException best = null;
		try {
			int prevIndex = -1;
			while (index < text.length() && index > prevIndex) {
				prevIndex = index;
				final Parser<R> parser = manyRule.getRule().createParser();
				index = parser.parse(text, index);
				if (index > prevIndex) {
					//celé signifie que l"index n a pas avancé, on sort
					results.add(parser.get());
				}
			}
		} catch (final NotFoundException e) {
			best = e;
			if (best.getIndex() > index) { //Si on a plus avancé avec une autre règle c'est que celle ci n'avance pas assez (typiquement une WhiteSpace seule, ou une OptionRule)
				throw best;
			}
		}
		if (!manyRule.isEmptyAccepted() && results.isEmpty()) {
			throw new NotFoundException(text, start, best, "Aucun élément de la liste trouvé : {0}", manyRule.getExpression());
		}
		if (manyRule.isRepeat() && text.length() > index) {
			throw new NotFoundException(text, start, best, "{0} élément(s) trouvé(s), éléments suivants non parsés selon la règle :{1}", results.size(), manyRule.getExpression());
		}
		return index;
	}

	@Override
	public List<R> get() {
		return results;
	}
}
