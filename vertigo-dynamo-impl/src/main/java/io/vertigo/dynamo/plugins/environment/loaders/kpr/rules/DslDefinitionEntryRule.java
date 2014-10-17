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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.PAIR_SEPARATOR;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.WORD;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.WORDS;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * règle de déclaration d'une champ référenéant une listes de clés.
 * @author pchretien
 */
public final class DslDefinitionEntryRule extends AbstractRule<DslDefinitionEntry, List<?>> {
	private final List<String> fieldNames;

	/**
	 * Constructeur.
	 */
	public DslDefinitionEntryRule(final List<String> fieldNames) {
		Assertion.checkNotNull(fieldNames);
		//----------------------------------------------------------------------
		this.fieldNames = fieldNames;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final List<Rule<?>> fieldNamesRules = new ArrayList<>();
		for (final String fieldName : fieldNames) {
			fieldNamesRules.add(new TermRule(fieldName));
		}
		//---------------------------------------------------------------------
		return new SequenceRule(//"DefinitionKey"
				new FirstOfRule(fieldNamesRules), //0
				SPACES,//
				PAIR_SEPARATOR,//
				SPACES,//
				new FirstOfRule(WORD, WORDS),//4
				SPACES,//
				new OptionRule<>(DslSyntaxRules.OBJECT_SEPARATOR)//
		);
	}

	@Override
	protected DslDefinitionEntry handle(final List<?> parsing) {
		final String fieldName = (String) ((Choice) parsing.get(0)).getResult();
		final List<String> definitionKeys;

		final Choice definitionChoice = (Choice) parsing.get(4);
		switch (definitionChoice.getValue()) {
			case 1:
				//Déclaration d'une liste de définitions identifiée par leurs clés
				definitionKeys = (List<String>) definitionChoice.getResult();
				break;
			case 0:
				//Déclaration d'une définition identifiée par sa clé
				final String value = (String) definitionChoice.getResult();
				definitionKeys = java.util.Collections.singletonList(value);
				break;
			default:
				throw new IllegalStateException();
		}
		return new DslDefinitionEntry(fieldName, definitionKeys);
	}
}
