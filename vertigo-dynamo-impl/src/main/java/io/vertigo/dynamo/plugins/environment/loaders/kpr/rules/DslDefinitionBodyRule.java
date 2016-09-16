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

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.OBJECT_END;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.OBJECT_START;
import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslSyntaxRules.SPACES;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.commons.peg.AbstractRule;
import io.vertigo.commons.peg.PegChoice;
import io.vertigo.commons.peg.PegRule;
import io.vertigo.commons.peg.PegRules;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.definition.dsl.entity.DslEntityField;
import io.vertigo.core.definition.dsl.entity.DslEntityLink;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;
import io.vertigo.lang.Assertion;

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
	private final DslEntity entity;

	/**
	 * Constructeur.
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	public DslDefinitionBodyRule(final DynamicDefinitionRepository dynamicModelRepository, final DslEntity entity) {
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
	protected PegRule<List<?>> createMainRule() {
		final List<String> attributeNames = new ArrayList<>();

		final List<PegRule<?>> innerDefinitionRules = new ArrayList<>();

		for (final DslEntityField dslEntityField : entity.getFields()) {
			attributeNames.add(dslEntityField.getName());

			final DslEntity dslEntity;
			if (dslEntityField.getType() instanceof DslEntity) {
				dslEntity = DslEntity.class.cast(dslEntityField.getType());
			} else if (dslEntityField.getType() instanceof DslEntityLink) {
				dslEntity = DslEntityLink.class.cast(dslEntityField.getType()).getEntity();
			} else {
				//case property
				dslEntity = null;
			}
			if (dslEntity != null) {
				innerDefinitionRules.add(new DslInnerDefinitionRule(dynamicModelRepository, dslEntityField.getName(), dslEntity));
			}
		}

		final DslPropertyEntryRule xPropertyEntryRule = new DslPropertyEntryRule(entity.getPropertyNames());
		final DslDefinitionEntryRule xDefinitionEntryRule = new DslDefinitionEntryRule(attributeNames);
		final PegRule<PegChoice> firstOfRule = PegRules.choice(
				xPropertyEntryRule, // 0
				xDefinitionEntryRule, // 1
				PegRules.choice(innerDefinitionRules), //2,
				SPACES);

		final PegRule<List<PegChoice>> manyRule = PegRules.zeroOrMore(firstOfRule, false);
		return PegRules.sequence(
				OBJECT_START,
				SPACES,
				manyRule, //2
				SPACES,
				OBJECT_END);
	}

	@Override
	protected DslDefinitionBody handle(final List<?> parsing) {
		final List<PegChoice> many = (List<PegChoice>) parsing.get(2);

		final List<DslDefinitionEntry> fieldDefinitionEntries = new ArrayList<>();
		final List<DslPropertyEntry> fieldPropertyEntries = new ArrayList<>();
		for (final PegChoice item : many) {
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
					final PegChoice subTuple = (PegChoice) item.getResult();
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
