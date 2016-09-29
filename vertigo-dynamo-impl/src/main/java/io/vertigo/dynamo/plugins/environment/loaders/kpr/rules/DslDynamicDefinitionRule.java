/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.definition.dsl.entity.DslGrammar;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.lang.Assertion;

/*
 * @author pchretien
 */
public final class DslDynamicDefinitionRule extends AbstractRule<DynamicDefinition, PegChoice> {

	/**
	 * Constructor.
	 * @param grammar the grammar
	 */
	public DslDynamicDefinitionRule(final String operation, final DslGrammar grammar) {
		super(createMainRule(operation, grammar), operation + "Definitions");
	}

	private static PegRule<PegChoice> createMainRule(final String operation, final DslGrammar grammar) {
		Assertion.checkArgNotEmpty(operation);
		Assertion.checkNotNull(grammar);
		//-----
		final List<PegRule<?>> rules = new ArrayList<>();//"Definition")
		for (final DslEntity entity : grammar.getEntities()) {
			final DslInnerDefinitionRule definitionRule = new DslInnerDefinitionRule(entity.getName(), entity);
			rules.add(createRule(operation, definitionRule));
		}
		return PegRules.choice(rules);
	}

	private static PegRule<List<?>> createRule(final String operation, final DslInnerDefinitionRule definitionRule) {
		// Création de la règle de déclaration d'une nouvelle definition.
		return PegRules.sequence(//Definition
				PegRules.term(operation), // alter ou create
				SPACES,
				definitionRule, //2
				SPACES);
	}

	@Override
	protected DynamicDefinition handle(final PegChoice parsing) {
		final DslDefinitionEntry xDefinitionEntry = (DslDefinitionEntry) ((List) parsing.getValue()).get(2);
		return xDefinitionEntry.getDefinition();
	}
}
