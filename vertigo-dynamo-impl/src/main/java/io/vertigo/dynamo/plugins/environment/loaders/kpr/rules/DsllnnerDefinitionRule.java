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

import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.OptionRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.commons.parser.TermRule;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;

import java.util.ArrayList;
import java.util.List;

final class DslInnerDefinitionRule extends AbstractRule<DslDefinitionEntry, List<?>> {
	private final DynamicDefinitionRepository dynamicModelRepository;
	private final String entityName;
	private final Entity entity;

	DslInnerDefinitionRule(final DynamicDefinitionRepository dynamicModelRepository, final String entityName, final Entity entity) {
		Assertion.checkNotNull(dynamicModelRepository);
		Assertion.checkArgNotEmpty(entityName);
		Assertion.checkNotNull(entity);
		//-----------------------------------------------------------------
		this.dynamicModelRepository = dynamicModelRepository;
		this.entityName = entityName;
		this.entity = entity;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final DslDefinitionBodyRule definitionBodyRule = new DslDefinitionBodyRule(dynamicModelRepository, entity);
		return new SequenceRule(//"InnerDefinition"
				new TermRule(entityName), //
				DslSyntaxRules.SPACES,//
				DslSyntaxRules.WORD,//2
				DslSyntaxRules.SPACES,//
				definitionBodyRule,//4
				DslSyntaxRules.SPACES,//
				new OptionRule<>(DslSyntaxRules.OBJECT_SEPARATOR)//
				);
	}

	@Override
	protected DslDefinitionEntry handle(final List<?> parsing) {
		//Dans le cas des sous définition :: field [PRD_XXX]

		final String definitionName = (String) parsing.get(2);
		final DslDefinitionBody definitionBody = (DslDefinitionBody) parsing.get(4);

		final DynamicDefinitionBuilder dynamicDefinitionBuilder = DynamicDefinitionRepository.createDynamicDefinitionBuilder(definitionName, entity, null);
		populateDefinition(definitionBody, dynamicDefinitionBuilder);

		//---
		return new DslDefinitionEntry(entityName, dynamicDefinitionBuilder.build());
	}

	/**
	 * Peuple la définition à partir des éléments trouvés.
	 */
	private static void populateDefinition(final DslDefinitionBody definitionBody, final DynamicDefinitionBuilder dynamicDefinitionBuilder) {
		for (final DslDefinitionEntry fieldDefinitionEntry : definitionBody.getDefinitionEntries()) {
			// ------------------------------------------------------------------
			// 1.On vérifie que le champ existe pour la metaDefinition
			// et qu'elle n'est pas déjà enregistrée sur l'objet.
			// ------------------------------------------------------------------
			if (fieldDefinitionEntry.containsDefinition()) {
				// On ajoute la définition par sa valeur.
				dynamicDefinitionBuilder.withChildDefinition(fieldDefinitionEntry.getFieldName(), fieldDefinitionEntry.getDefinition());
			} else {
				// On ajoute les définitions par leur clé.
				dynamicDefinitionBuilder.withDefinitions(fieldDefinitionEntry.getFieldName(), toDefinitionKeys(fieldDefinitionEntry.getDefinitionKeys()));
			}
		}
		for (final DslPropertyEntry fieldPropertyEntry : definitionBody.getPropertyEntries()) {
			//			// On vérifie que la propriété est enregistrée sur la metaDefinition
			//			Assertion.precondition(definition.getEntity().getPropertySet().contains(fieldPropertyEntry.getProperty()), "Propriété {0} non enregistré sur {1}",
			//					fieldPropertyEntry.getProperty(), definition.getEntity().getName());
			//			// ------------------------------------------------------------------
			final Object value = readProperty(fieldPropertyEntry.getProperty(), fieldPropertyEntry.getPropertyValueAsString());
			dynamicDefinitionBuilder.withPropertyValue(fieldPropertyEntry.getProperty(), value);
		}
	}

	//=========================================================================
	//=================================STATIC==================================
	//=========================================================================

	/**
	 * Retourne la valeur typée en fonction de son expression sous forme de String
	 * L'expression est celle utilisée dans le fichier xml/ksp.
	 * Cette méthode n'a pas besoin d'être optimisée elle est appelée au démarrage uniquement.
	 * @param property Propriété à lire.
	 * @param stringValue Valeur de la propriété sous forme String
	 * @return J Valeur typée de la propriété
	 */
	private static Object readProperty(final EntityProperty property, final String stringValue) {
		Assertion.checkNotNull(property);
		//---------------------------------------------------------------------
		return property.getPrimitiveType().cast(stringValue);
	}

	private static List<DynamicDefinitionKey> toDefinitionKeys(final List<String> list) {
		final List<DynamicDefinitionKey> definitionKeys = new ArrayList<>();
		for (final String item : list) {
			definitionKeys.add(new DynamicDefinitionKey(item));
		}
		return definitionKeys;
	}

}
