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
package io.vertigo.core.definition.dsl.dynamic;

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityLink;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Classe permettant de créer dynamiquement une structure grammaticale.
 * Cette Classe est utilisée pour parcourir dynamiquement les modèles.
 * Rappelons qu'une structure est elle-même composée de sous structure grammaticales.
 *
 * @author  pchretien
 */
final class DynamicDefinitionImpl implements DynamicDefinitionBuilder, DynamicDefinition {
	/** Type. */
	private final Entity entity;

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

	private final Map<String, List<String>> definitionNamesByFieldName = new LinkedHashMap<>();

	/** 
	 * Children.
	 * Map (fieldName, definitions
	 */
	private final Map<String, List<DynamicDefinition>> definitionsByFieldName = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param dynamicDefinitionKey Clé de la définition
	 * @param entity Entité
	 */
	DynamicDefinitionImpl(final String dynamicDefinitionName, final Entity entity) {
		Assertion.checkNotNull(dynamicDefinitionName);
		Assertion.checkNotNull(entity);
		//-----
		this.name = dynamicDefinitionName;
		this.entity = entity;
	}

	/** {@inheritDoc} */
	@Override
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	@Override
	public Entity getEntity() {
		return entity;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinition build() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public Object getPropertyValue(final String fieldName) {
		Assertion.checkNotNull(fieldName);
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
	public final List<String> getDefinitionNames(final String fieldName) {
		return obtainList(definitionNamesByFieldName, fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public final List<DynamicDefinition> getChildDefinitions(final String fieldName) {
		return obtainList(definitionsByFieldName, fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public final List<DynamicDefinition> getAllChildDefinitions() {
		final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();
		for (final List<DynamicDefinition> dynamicDefinitionList : definitionsByFieldName.values()) {
			dynamicDefinitions.addAll(dynamicDefinitionList);
		}
		return dynamicDefinitions;
	}

	/** {@inheritDoc} */
	@Override
	public final boolean containsDefinitionName(final String fieldName) {
		return definitionNamesByFieldName.containsKey(fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public final String getDefinitionName(final String fieldName) {
		Assertion.checkArgument(containsDefinitionName(fieldName), "Aucune définition déclarée pour ''{0}'' sur ''{1}'' ", fieldName, getName());
		final List<String> list = definitionNamesByFieldName.get(fieldName);
		final String definitionName = list.get(0);
		//-----
		// On vérifie qu'il y a une définition pour le champ demandé
		Assertion.checkNotNull(definitionName);
		return definitionName;
	}

	/** {@inheritDoc} */
	@Override
	public final List<String> getAllDefinitionNames() {
		final List<String> allDefinitionNames = new ArrayList<>();
		for (final List<String> dynamicDefinitionNames : definitionNamesByFieldName.values()) {
			allDefinitionNames.addAll(dynamicDefinitionNames);
		}
		return allDefinitionNames;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder withPackageName(final String newPackageName) {
		packageName = newPackageName;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addPropertyValue(final String propertyName, final Object value) {
		getEntity().getPrimitiveType(propertyName).checkValue(value);
		propertyValueByFieldName.put(propertyName, value);
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
	public final DynamicDefinitionBuilder addDefinition(final String fieldName, final DynamicDefinition definition) {
		addAllChildrenDefinition(fieldName, Collections.singletonList(definition));
		return this;
	}

	private final void addAllChildrenDefinition(final String fieldName, final List<DynamicDefinition> definitions) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definitions);
		Assertion.checkState(entity.getAttribute(fieldName).getType() instanceof Entity,
				"expected a pure entity on {0}", fieldName);
		//-----
		obtainList(definitionsByFieldName, fieldName).addAll(definitions);
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addAllDefinitions(final String fieldName, final List<String> definitionNames) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definitionNames);
		Assertion.checkState(entity.getAttribute(fieldName).getType() instanceof EntityLink,
				"expected a link on {0}", fieldName);
		//-----
		obtainList(definitionNamesByFieldName, fieldName).addAll(definitionNames);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addDefinition(final String fieldName, final String definitionName) {
		return addAllDefinitions(fieldName, Collections.singletonList(definitionName));
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addBody(final DynamicDefinition dynamicDefinition) {
		// 1. maj des EntityProperty
		for (final String propertyName : dynamicDefinition.getPropertyNames()) {
			addPropertyValue(propertyName, dynamicDefinition.getPropertyValue(propertyName));
		}

		// 2. maj fieldNameDefinitionKeyListMap
		final DynamicDefinitionImpl other = (DynamicDefinitionImpl) dynamicDefinition;

		for (final Entry<String, List<String>> entry : other.definitionNamesByFieldName.entrySet()) {
			final String fieldName = entry.getKey();
			final List<String> definitionNames = entry.getValue();
			//-----
			addAllDefinitions(fieldName, definitionNames);
		}

		// 3.
		for (final Entry<String, List<DynamicDefinition>> entry : other.definitionsByFieldName.entrySet()) {
			final String fieldName = entry.getKey();
			final List<DynamicDefinition> definitions = entry.getValue();
			//-----
			addAllChildrenDefinition(fieldName, definitions);
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}

}
