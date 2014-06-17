package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.ARRAY_END;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.ARRAY_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.ARRAY_START;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.WORD;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;

import java.util.ArrayList;
import java.util.List;

/**
 * règle de composition d'une déclaration de liste de mots.
 * {mot1, mot2, mot3}
 * @author pchretien
 */
public final class WordListRule extends AbstractRule<List<String>, Choice> {

	// 	{ } 
	private static final Rule<List<?>> EMPTY_LIST = new SequenceRule(//Liste vide
			ARRAY_START,//
			SPACES,//
			ARRAY_END);

	// , XXXX 
	private static final Rule<List<List<?>>> MANY_WORDS = new ManyRule<>(//
			new SequenceRule(//"mot"
					ARRAY_SEPARATOR, //
					SPACES, //
					WORD //2
			), true);

	//{ XXXXX (,XXXX)+ }
	private static final Rule<List<?>> NON_EMPTY_LIST = new SequenceRule(//"Liste non vide"
			ARRAY_START,//
			SPACES,//
			WORD,//2
			MANY_WORDS, // 3
			SPACES,//
			ARRAY_END);

	@Override
	// {} | { XXXXX (,XXXX)+ }
	protected Rule<Choice> createMainRule() {
		return new FirstOfRule(//"liste vide ou non"
				EMPTY_LIST, //0
				NON_EMPTY_LIST);//1 
	}

	@Override
	protected List<String> handle(final Choice parsing) {
		final List<String> words = new ArrayList<>();
		//---
		switch (parsing.getValue()) {
			case 0: //liste vide on s'arrète
				break;
			case 1: //liste non vide on continue
				//On récupère le prmier mot qui est obligatoire.
				final List<?> list = (List<?>) parsing.getResult();
				words.add((String) list.get(2));
				//On récupère le produit de la règle many
				final List<List<?>> many = (List<List<?>>) list.get(3);
				for (final List<?> row : many) {
					words.add((String) row.get(2));
				}
				break;
			default:
				throw new IllegalArgumentException("case " + parsing.getValue() + " not implemented");
		}
		return words;
	}
}
