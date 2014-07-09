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
package io.vertigo.dynamo.impl.environment.kernel.model;

import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.kernel.lang.Builder;

import java.util.List;

/**
 * Interface de création des définitions.
 * @author  pchretien
 */
public interface DynamicDefinitionBuilder extends Builder<DynamicDefinition> {
	DynamicDefinitionBuilder withPackageName(String packageName);

	DynamicDefinitionBuilder withBody(DynamicDefinition dynamicDefinition);

	/**
	 * @param property Propriété
	 * @param value Valeur de la propriété
	 */
	DynamicDefinitionBuilder withPropertyValue(final EntityProperty property, final Object value);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKey Clé de la définition
	 */
	DynamicDefinitionBuilder withDefinition(final String fieldName, final DynamicDefinitionKey definitionKey);

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKeyList  Liste des clés de définition
	 */
	DynamicDefinitionBuilder withDefinitions(final String fieldName, final List<DynamicDefinitionKey> definitionKeys);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	DynamicDefinitionBuilder withChildDefinition(final String fieldName, final DynamicDefinition definition);
}
