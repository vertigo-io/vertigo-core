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
package io.vertigo.dynamox.search.dsl.rules;

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamox.search.dsl.definition.DslFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiFieldDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsing rule for query.
 * //(preMultiField)[(fields)+,](postMultiField)
 * @author npiedeloup
 */
public final class DslMultiFieldRule extends AbstractRule<DslMultiFieldDefinition, List<?>> {
	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<List<List<?>>> otherFieldsRule = new ManyRule<>(
				new SequenceRule(
						DslSyntaxRules.ARRAY_SEPARATOR,
						DslSyntaxRules.SPACES,
						new DslFieldRule() //2
				), true);

		return new SequenceRule(
				DslSyntaxRules.ARRAY_START,
				DslSyntaxRules.SPACES,
				new DslFieldRule(),//2
				otherFieldsRule, //3
				DslSyntaxRules.SPACES,
				DslSyntaxRules.ARRAY_END);
	}

	@Override
	protected DslMultiFieldDefinition handle(final List<?> parsing) {
		final String preMultiField = "";//(String) parsing.get(0);
		final List<DslFieldDefinition> fields = new ArrayList<>();
		//---
		//On récupère le premier mot qui est obligatoire.
		final DslFieldDefinition firstField = (DslFieldDefinition) parsing.get(2);
		fields.add(firstField);
		//On récupère le produit de la règle many
		final List<List<?>> many = (List<List<?>>) parsing.get(3);
		for (final List<?> row : many) {
			fields.add((DslFieldDefinition) row.get(2));
		}
		//---
		final String postMultiField = "";//(String) parsing.get(7);
		return new DslMultiFieldDefinition(preMultiField, fields, postMultiField);
	}
}
