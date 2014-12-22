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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/*
 * @author pchretien
 */
public final class DslDynamicDefinitionRule extends AbstractRule<DynamicDefinition, Choice> {
	/** Création de la définition. */
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final String operation;

	/**
	 * Constructeur.
	 *
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	public DslDynamicDefinitionRule(final String operation, final DynamicDefinitionRepository dynamicModelRepository) {
		Assertion.checkArgNotEmpty(operation);
		Assertion.checkNotNull(dynamicModelRepository);
		//-----
		this.operation = operation;
		this.dynamicModelRepository = dynamicModelRepository;
	}

	private Rule<List<?>> createRule(final DslInnerDefinitionRule definitionRule) {
		// Création de la règle de déclaration d'une nouvelle definition.
		return new SequenceRule(//Definition
				new TermRule(operation),// alter ou create
				SPACES,
				definitionRule,//2
				SPACES);
	}

	@Override
	protected Rule<Choice> createMainRule() {
		final List<Rule<?>> rules = new ArrayList<>();//"Definition")
		for (final Entity entity : dynamicModelRepository.getGrammar().getEntities()) {
			final DslInnerDefinitionRule definitionRule = new DslInnerDefinitionRule(dynamicModelRepository, entity.getName(), entity);
			rules.add(createRule(definitionRule));
		}
		return new FirstOfRule(rules);
	}

	@Override
	protected DynamicDefinition handle(final Choice parsing) {
		final DslDefinitionEntry xDefinitionEntry = (DslDefinitionEntry) ((List) parsing.getResult()).get(2);
		return xDefinitionEntry.getDefinition();
	}
}
