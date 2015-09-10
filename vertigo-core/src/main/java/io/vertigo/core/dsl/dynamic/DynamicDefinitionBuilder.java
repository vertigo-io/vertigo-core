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
package io.vertigo.core.dsl.dynamic;

import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.lang.Builder;

import java.util.List;

/**
 * Interface de création des définitions.
 * @author  pchretien
 */
public interface DynamicDefinitionBuilder extends Builder<DynamicDefinition> {
	Entity getEntity();

	/**
	 * @param packageName Package name
	 * @return Builder
	 */
	DynamicDefinitionBuilder withPackageName(String packageName);

	/**
	 * @param dynamicDefinition Definition body
	 * @return Builder
	 */
	DynamicDefinitionBuilder addBody(DynamicDefinition dynamicDefinition);

	/**
	 * @param fieldName Nom du champ
	 * @param value Valeur de la propriété
	 * @return Builder
	 */
	DynamicDefinitionBuilder addPropertyValue(final String fieldName, final Object value);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKey Clé de la définition
	 * @return Builder
	 */
	DynamicDefinitionBuilder addDefinition(final String fieldName, final String definitionName);

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKeys  Liste des clés de définition
	 * @return Builder
	 */
	DynamicDefinitionBuilder addAllDefinitions(final String fieldName, final List<String> definitionNames);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 * @return Builder
	 */
	DynamicDefinitionBuilder addDefinition(final String fieldName, final DynamicDefinition definition);
}
