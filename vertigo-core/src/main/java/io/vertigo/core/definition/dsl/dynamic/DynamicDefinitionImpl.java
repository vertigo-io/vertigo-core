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
import java.util.Map.Entry;
import java.util.Set;

import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.lang.Assertion;

/**
 * Classe permettant de créer dynamiquement une structure grammaticale.
 * Cette Classe est utilisée pour parcourir dynamiquement les modèles.
 * Rappelons qu'une structure est elle-même composée de sous structure grammaticales.
 *
 * @author  pchretien
 */
final class DynamicDefinitionImpl implements DynamicDefinitionBuilder, DynamicDefinition {
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
	DynamicDefinitionImpl(final String name, final DslEntity entity) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(entity);
		//-----
		this.name = name;
		this.entity = entity;
	}

	/** {@inheritDoc} */
	@Override
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	@Override
	public DslEntity getEntity() {
		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinition build() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public Object getPropertyValue(final String fieldName) {
		assertThatFieldIsAProperty(fieldName);
		// On ne vérifie rien sur le type retourné par le getter.
		// le type a été validé lors du put.
		//-----
		// Conformémément au contrat, on retourne null si pas de propriété trouvée
		return propertyValueByFieldName.get(fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(propertyValueByFieldName.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public List<DynamicDefinition> getChildDefinitions(final String fieldName) {
		assertThatFieldIsAnEntity(fieldName);
		//---
		return obtainList(childDefinitionsByFieldName, fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public List<DynamicDefinition> getAllChildDefinitions() {
		final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();
		for (final List<DynamicDefinition> dynamicDefinitionList : childDefinitionsByFieldName.values()) {
			dynamicDefinitions.addAll(dynamicDefinitionList);
		}
		return dynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsDefinitionLinkName(final String fieldName) {
		assertThatFieldIsALink(fieldName);
		//---
		return definitionLinkNamesByFieldName.containsKey(fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getDefinitionLinkNames(final String fieldName) {
		assertThatFieldIsALink(fieldName);
		//---
		return obtainList(definitionLinkNamesByFieldName, fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public String getDefinitionLinkName(final String fieldName) {
		assertThatFieldIsALink(fieldName);
		Assertion.checkArgument(containsDefinitionLinkName(fieldName), "Aucun lien déclaré pour le champ ''{0}'' sur ''{1}'' ", fieldName, getName());
		final List<String> list = definitionLinkNamesByFieldName.get(fieldName);
		final String definitionName = list.get(0);
		//-----
		// On vérifie qu'il y a une définition pour le champ demandé
		Assertion.checkNotNull(definitionName);
		return definitionName;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getAllDefinitionLinkFieldNames() {
		return new ArrayList<>(definitionLinkNamesByFieldName.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder withPackageName(final String newPackageName) {
		packageName = newPackageName;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder addPropertyValue(final String fieldName, final Object value) {
		assertThatFieldIsAProperty(fieldName);
		//----
		getEntity().getPropertyType(fieldName).checkValue(value);
		propertyValueByFieldName.put(fieldName, value);
		return this;
	}

	private static <K, V> List<V> obtainList(final Map<K, List<V>> map, final K key) {
		Assertion.checkNotNull(map);
		Assertion.checkNotNull(key);
		//-----
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<>();
			map.put(key, list);
		}
		return list;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder addChildDefinition(final String fieldName, final DynamicDefinition definition) {
		addAllChildDefinitions(fieldName, Collections.singletonList(definition));
		return this;
	}

	private void addAllChildDefinitions(final String fieldName, final List<DynamicDefinition> definitions) {
		assertThatFieldIsAnEntity(fieldName);
		Assertion.checkNotNull(definitions);
		//-----
		obtainList(childDefinitionsByFieldName, fieldName).addAll(definitions);
	}

	private void assertThatFieldIsAProperty(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkState(entity.getField(fieldName).getType().isProperty(),
				"expected a property on {0}", fieldName);
	}

	private void assertThatFieldIsAnEntity(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkState(entity.getField(fieldName).getType().isEntity(),
				"expected an entity on {0}", fieldName);
	}

	private void assertThatFieldIsALink(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkState(entity.getField(fieldName).getType().isEntityLink(),
				"expected a link on {0}", fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder addAllDefinitionLinks(final String fieldName, final List<String> definitionNames) {
		assertThatFieldIsALink(fieldName);
		Assertion.checkNotNull(definitionNames);
		//-----
		obtainList(definitionLinkNamesByFieldName, fieldName).addAll(definitionNames);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder addDefinitionLink(final String fieldName, final String definitionName) {
		return addAllDefinitionLinks(fieldName, Collections.singletonList(definitionName));
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder merge(final DynamicDefinition dynamicDefinition) {
		// 1. maj des EntityProperty
		for (final String propertyName : dynamicDefinition.getPropertyNames()) {
			addPropertyValue(propertyName, dynamicDefinition.getPropertyValue(propertyName));
		}

		// 2. maj fieldNameDefinitionKeyListMap
		final DynamicDefinitionImpl other = (DynamicDefinitionImpl) dynamicDefinition;

		for (final Entry<String, List<String>> entry : other.definitionLinkNamesByFieldName.entrySet()) {
			final String fieldName = entry.getKey();
			final List<String> definitionNames = entry.getValue();
			//-----
			addAllDefinitionLinks(fieldName, definitionNames);
		}

		// 3.
		for (final Entry<String, List<DynamicDefinition>> entry : other.childDefinitionsByFieldName.entrySet()) {
			final String fieldName = entry.getKey();
			final List<DynamicDefinition> definitions = entry.getValue();
			//-----
			addAllChildDefinitions(fieldName, definitions);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

}
