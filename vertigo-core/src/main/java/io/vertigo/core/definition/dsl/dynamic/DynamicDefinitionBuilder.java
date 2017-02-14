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
package io.vertigo.core.definition.dsl.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.definition.dsl.entity.DslEntityField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Interface de création des définitions.
 * @author  pchretien
 */
public final class DynamicDefinitionBuilder implements Builder<DynamicDefinition> {
	/** Type. */
	private final DslEntity entity;

	/** Name of the package. */
	private String packageName;

	/**name of this definition.*/
	private final String name;

	/** Map  (fieldName, propertyValue)  */
	private final Map<String, Object> propertyValueByFieldName = new HashMap<>();

	/**
	 * Links.
	 * Map (fieldName, definitions identified by its name)
	 */

	private final Map<String, List<String>> definitionLinkNamesByFieldName = new LinkedHashMap<>();

	/**
	 * Children.
	 * Map (fieldName, definitions
	 */
	private final Map<String, List<DynamicDefinition>> childDefinitionsByFieldName = new LinkedHashMap<>();

	/**
	 * Constructor.
	 * @param name the name of the dynamicDefinition
	 * @param entity Entité
	 */
	public DynamicDefinitionBuilder(final String name, final DslEntity entity) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(entity);
		//-----
		this.name = name;
		this.entity = entity;
		for (final DslEntityField dslEntityField : entity.getFields()) {
			if (dslEntityField.getType().isEntityLink()) {
				definitionLinkNamesByFieldName.put(dslEntityField.getName(), new ArrayList<>());
			} else if (dslEntityField.getType().isEntity()) {
				childDefinitionsByFieldName.put(dslEntityField.getName(), new ArrayList<>());
			}
		}
	}

	public DslEntity getEntity() {
		return entity;
	}

	/**
	 * @param packageName Package name
	 * @return Builder
	 */
	public DynamicDefinitionBuilder withPackageName(final String newPackageName) {
		packageName = newPackageName;
		return this;
	}

	/**
	 * @param dynamicDefinition Definition body
	 * @return this builder
	 */
	public DynamicDefinitionBuilder merge(final DynamicDefinition dynamicDefinition) {
		if (packageName == null) {
			withPackageName(dynamicDefinition.getPackageName());
		}
		// 1. maj des EntityProperty
		for (final String propertyName : dynamicDefinition.getPropertyNames()) {
			addPropertyValue(propertyName, dynamicDefinition.getPropertyValue(propertyName));
		}

		for (final DslEntityField dslEntityField : entity.getFields()) {
			if (dslEntityField.getType().isEntityLink()) {
				// 2. link
				addAllDefinitionLinks(dslEntityField.getName(), dynamicDefinition.getDefinitionLinkNames(dslEntityField.getName()));
			} else if (dslEntityField.getType().isEntity()) {
				// 3. child
				addAllChildDefinitions(dslEntityField.getName(), dynamicDefinition.getChildDefinitions(dslEntityField.getName()));
			}
		}
		return this;
	}

	/**
	 * @param fieldName Name of the field
	 * @param value Valeur de la propriété
	 * @return this builder
	 */
	public DynamicDefinitionBuilder addPropertyValue(final String fieldName, final Object value) {
		entity.assertThatFieldIsAProperty(fieldName);
		//----
		getEntity().getPropertyType(fieldName).checkValue(value);
		propertyValueByFieldName.put(fieldName, value);
		return this;
	}

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Name of the field
	 * @param definitionName Name of the definition
	 * @return this builder
	 */
	public DynamicDefinitionBuilder addDefinitionLink(final String fieldName, final String definitionName) {
		return addAllDefinitionLinks(fieldName, Collections.singletonList(definitionName));
	}

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Name of the field
	 * @param definitionNames  list of the names of the dedinitions
	 * @return this builder
	 */
	public DynamicDefinitionBuilder addAllDefinitionLinks(final String fieldName, final List<String> definitionNames) {
		entity.assertThatFieldIsALink(fieldName);
		Assertion.checkNotNull(definitionNames);
		//-----
		definitionLinkNamesByFieldName.get(fieldName).addAll(definitionNames);
		return this;
	}

	private void addAllChildDefinitions(final String fieldName, final List<DynamicDefinition> definitions) {
		entity.assertThatFieldIsAnEntity(fieldName);
		Assertion.checkNotNull(definitions);
		//-----
		childDefinitionsByFieldName.get(fieldName).addAll(definitions);
	}

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Name of the field
	 * @param definition Définition
	 * @return this builder
	 */
	public DynamicDefinitionBuilder addChildDefinition(final String fieldName, final DynamicDefinition definition) {
		addAllChildDefinitions(fieldName, Collections.singletonList(definition));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinition build() {
		return new DynamicDefinition(entity, packageName, name, propertyValueByFieldName, definitionLinkNamesByFieldName, childDefinitionsByFieldName);
	}

}
