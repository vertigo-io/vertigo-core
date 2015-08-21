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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.OBJECT_END;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.OBJECT_START;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityField;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Règle définissant le corps d'une définition dynamo.
 * Une définition est composée d'une liste de
 * - couple (propriété, valeur)
 * - couple (champ, définition(s)).
 * Une définition étant soit affectée en ligne soit référencée.
 *
 * @author pchretien
 */
public final class DslDefinitionBodyRule extends AbstractRule<DslDefinitionBody, List<?>> {
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final Entity entity;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	public DslDefinitionBodyRule(final DynamicDefinitionRepository dynamicModelRepository, final Entity entity) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkNotNull(entity);
		//-----
		this.dynamicModelRepository = dynamicModelRepository;
		this.entity = entity;
	}

	/** {@inheritDoc} */
	@Override
	public String getExpression() {
		return "definition<" + entity.getName() + ">";
	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final List<String> attributeNames = new ArrayList<>();

		final List<Rule<?>> innerDefinitionRules = new ArrayList<>();

		for (final EntityField attribute : entity.getAttributes()) {
			final String attributeName = attribute.getName();
			attributeNames.add(attributeName);
			innerDefinitionRules.add(new DslInnerDefinitionRule(dynamicModelRepository, attributeName, (Entity) attribute.getType()));
		}

		final DslPropertyEntryRule xPropertyEntryRule = new DslPropertyEntryRule(entity.getPropertyNames());
		final DslDefinitionEntryRule xDefinitionEntryRule = new DslDefinitionEntryRule(attributeNames);
		final FirstOfRule firstOfRule = new FirstOfRule(
				xPropertyEntryRule, // 0
				xDefinitionEntryRule, // 1
				new FirstOfRule(innerDefinitionRules),//2,
				SPACES);

		final ManyRule<Choice> manyRule = new ManyRule<>(firstOfRule, true);
		return new SequenceRule(
				OBJECT_START,
				SPACES,
				manyRule,//2
				SPACES,
				OBJECT_END);
	}

	@Override
	protected DslDefinitionBody handle(final List<?> parsing) {
		final List<Choice> many = (List<Choice>) parsing.get(2);

		final List<DslDefinitionEntry> fieldDefinitionEntries = new ArrayList<>();
		final List<DslPropertyEntry> fieldPropertyEntries = new ArrayList<>();
		for (final Choice item : many) {
			switch (item.getValue()) {
				case 0:
					//Soit on est en présence d'une propriété standard
					final DslPropertyEntry propertyEntry = (DslPropertyEntry) item.getResult();
					fieldPropertyEntries.add(propertyEntry);
					break;
				case 1:
					final DslDefinitionEntry xDefinitionEntry = (DslDefinitionEntry) item.getResult();
					fieldDefinitionEntries.add(xDefinitionEntry);
					break;
				case 2:
					final Choice subTuple = (Choice) item.getResult();
					fieldDefinitionEntries.add((DslDefinitionEntry) subTuple.getResult());
					break;
				case 3:
					break;
				default:
					throw new IllegalArgumentException("Type of rule not supported");
			}
		}
		return new DslDefinitionBody(fieldDefinitionEntries, fieldPropertyEntries);
	}
}
