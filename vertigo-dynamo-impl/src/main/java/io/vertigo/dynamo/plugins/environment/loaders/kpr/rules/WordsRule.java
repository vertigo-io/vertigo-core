/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public final class WordsRule extends AbstractRule<List<String>, Choice> {

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
