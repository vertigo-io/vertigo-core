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
package io.vertigo.dynamo.impl.environment.kernel.impl.model;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionKey;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
	/** Type de l'objet. */
	private final Entity entity;

	/** Nom du package. */
	private String packageName;
	//-----
	//-----BODY
	//-----
	/**
	 * Clé de la définition.
	 */
	private final DynamicDefinitionKey dynamicDefinitionKey;
	/**
	 * Conteneur des couples (propriétés, valeur)
	 */
	private final Map<EntityProperty, Object> properties = new HashMap<>();
	/**
	 * Map des (FieldName, definitionKeyList)
	 */
	private final Map<String, List<DynamicDefinitionKey>> definitionKeysByFieldName = new LinkedHashMap<>();
	private final Map<String, List<DynamicDefinition>> definitionsByFieldName = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param dynamicDefinitionKey Clé de la définition
	 * @param packageName Nom du package
	 * @param entity Entité
	 */
	DynamicDefinitionImpl(final DynamicDefinitionKey dynamicDefinitionKey, final Entity entity) {
		Assertion.checkNotNull(dynamicDefinitionKey);
		//packageName peut être null
		Assertion.checkNotNull(entity);
		// -------------------------------------------------------------------------
		this.dynamicDefinitionKey = dynamicDefinitionKey;
		this.entity = entity;
	}

	/** {@inheritDoc} */
	public String getPackageName() {
		return packageName;
	}

	/** {@inheritDoc} */
	public Entity getEntity() {
		return entity;
	}

	/** {@inheritDoc} */
	public void check() {
		check(getEntity());
	}

	/** {@inheritDoc} */
	public DynamicDefinition build() {
		return this;
	}

	/** {@inheritDoc} */
	public final DynamicDefinitionKey getDefinitionKey() {
		return dynamicDefinitionKey;
	}

	/** {@inheritDoc} */
	public Object getPropertyValue(final EntityProperty property) {
		Assertion.checkNotNull(property);
		// On ne vérifie rien sur le type retourné par le getter.
		// le type a été validé lors du put.
		// ----------------------------------------------------------------------
		// Conformémément au contrat, on retourne null si pas de propriété
		// trouvée
		return properties.get(property);
	}

	/** {@inheritDoc} */
	public Set<EntityProperty> getProperties() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	/** {@inheritDoc} */
	public final List<DynamicDefinitionKey> getDefinitionKeys(final String fieldName) {
		return obtainDefinitionKeys(fieldName);
	}

	/** {@inheritDoc} */
	public final List<DynamicDefinition> getChildDefinitions(final String fieldName) {
		return obtainCompositeList(fieldName);
	}

	/** {@inheritDoc} */
	public final List<DynamicDefinition> getAllChildDefinitions() {
		final List<DynamicDefinition> dynamicDefinitions = new ArrayList<>();
		for (final List<DynamicDefinition> dynamicDefinitionList : definitionsByFieldName.values()) {
			dynamicDefinitions.addAll(dynamicDefinitionList);
		}
		return dynamicDefinitions;
	}

	//-------
	/** {@inheritDoc} */
	public final DynamicDefinitionKey getDefinitionKey(final String fieldName) {
		Assertion.checkArgument(definitionKeysByFieldName.containsKey(fieldName), "Aucune définition déclarée pour ''{0}'' sur ''{1}'' ", fieldName, getDefinitionKey().getName());
		final List<DynamicDefinitionKey> list = definitionKeysByFieldName.get(fieldName);
		final DynamicDefinitionKey definitionKey = list.get(0);
		// ------------------------------------------------------------------
		// On vérifie qu'il y a une définition pour le champ demandé
		Assertion.checkNotNull(definitionKey);
		return definitionKey;
	}

	/** {@inheritDoc} */
	public final List<DynamicDefinitionKey> getAllDefinitionKeys() {
		final List<DynamicDefinitionKey> dynamicDefinitionKeys = new ArrayList<>();
		for (final List<DynamicDefinitionKey> dynamicDefinitionKeyList : definitionKeysByFieldName.values()) {
			dynamicDefinitionKeys.addAll(dynamicDefinitionKeyList);
		}
		return dynamicDefinitionKeys;
	}

	/** {@inheritDoc} */
	public DynamicDefinitionBuilder withPackageName(final String newPackageName) {
		this.packageName = newPackageName;
		return this;
	}

	public final DynamicDefinitionBuilder withPropertyValue(final EntityProperty property, final Object value) {
		property.getPrimitiveType().checkValue(value);
		properties.put(property, value);
		return this;
	}

	private List<DynamicDefinitionKey> obtainDefinitionKeys(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		// ------------------------------------------------------------------
		List<DynamicDefinitionKey> list = definitionKeysByFieldName.get(fieldName);
		// ------------------------------------------------------------------
		if (list == null) {
			list = new ArrayList<>();
			definitionKeysByFieldName.put(fieldName, list);
		}
		return list;
	}

	public final DynamicDefinitionBuilder withChildDefinition(final String fieldName, final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		// ------------------------------------------------------------------
		obtainCompositeList(fieldName).add(definition);
		return this;
	}

	private void doAddDefinition(final String fieldName, final DynamicDefinitionKey definitionKey) {
		Assertion.checkNotNull(definitionKey);
		// ----------------------------------------------------------------------
		obtainDefinitionKeys(fieldName).add(definitionKey);
	}

	public final DynamicDefinitionBuilder withDefinitions(final String fieldName, final List<DynamicDefinitionKey> definitionKeys) {
		Assertion.checkNotNull(definitionKeys);
		Assertion.checkArgument(obtainDefinitionKeys(fieldName).isEmpty(), "syntaxe interdite");
		// On vérifie que la liste est vide pour éviter les syntaxe avec multi
		// déclarations
		// ----------------------------------------------------------------------
		for (final DynamicDefinitionKey definitionKey : definitionKeys) {
			doAddDefinition(fieldName, definitionKey);
		}
		return this;
	}

	private List<DynamicDefinition> obtainCompositeList(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		// ------------------------------------------------------------------
		List<DynamicDefinition> list = definitionsByFieldName.get(fieldName);
		// ------------------------------------------------------------------
		if (list == null) {
			list = new ArrayList<>();
			definitionsByFieldName.put(fieldName, list);
		}
		return list;
	}

	public final DynamicDefinitionBuilder withBody(final DynamicDefinition dynamicDefinition) {
		// 1. maj des EntityProperty
		for (final EntityProperty property : dynamicDefinition.getProperties()) {
			withPropertyValue(property, dynamicDefinition.getPropertyValue(property));
		}

		// 2. maj fieldNameDefinitionKeyListMap
		final DynamicDefinitionImpl other = (DynamicDefinitionImpl) dynamicDefinition;

		for (final Entry<String, List<DynamicDefinitionKey>> entry : other.definitionKeysByFieldName.entrySet()) {
			obtainDefinitionKeys(entry.getKey()).addAll(entry.getValue());
		}

		// 3.
		for (final Entry<String, List<DynamicDefinition>> entry : other.definitionsByFieldName.entrySet()) {
			obtainCompositeList(entry.getKey()).addAll(entry.getValue());
		}
		return this;
	}

	public DynamicDefinitionBuilder withDefinition(final String fieldName, final DynamicDefinitionKey definitionKey) {
		// On vérifie que la liste est vide pour éviter les syntaxe avec multi
		// déclarations
		Assertion.checkArgument(obtainDefinitionKeys(fieldName).isEmpty(), "syntaxe interdite");
		// ----------------------------------------------------------------------
		doAddDefinition(fieldName, definitionKey);
		return this;
	}

	public void check(final Entity myEntity) {
		Assertion.checkNotNull(myEntity);
		// ---------------------------------------------------------------------
		// 1.On vérifie la définition par rapport à la métadéfinition
		// 1.1 on vérifie les propriétés.
		final Set<EntityProperty> propertySet = getProperties();
		final Set<EntityProperty> metaDefinitionPropertySet = myEntity.getProperties();
		// 1.1.1 on vérifie que toutes les propriétés sont déclarées sur le
		// métamodèle
		checkProperties(propertySet, metaDefinitionPropertySet);

		// 1.1.2 on vérifie les propriétés obligatoires
		checkMandatoryProperties(myEntity, propertySet, metaDefinitionPropertySet);

		// 1.1.3 on vérifie les types des propriétés déclarées
		for (final EntityProperty prop : propertySet) {
			prop.getPrimitiveType().checkValue(getPropertyValue(prop));
		}

		// 1.2 on vérifie les définitions composites (sous définitions).
		for (final DynamicDefinition dynamicDefinition : getAllChildDefinitions()) {
			dynamicDefinition.check();
		}

		// 1.3 on vérifie les définitions références.
		// TODO vérifier les définitions références
	}

	private void checkProperties(final Set<EntityProperty> propertySet, final Set<EntityProperty> metaDefinitionPropertySet) {
		// Vérification que toutes les propriétés sont déclarées sur le
		// métamodèle
		Set<EntityProperty> undeclaredPropertySet = null;
		for (final EntityProperty property : propertySet) {
			if (!metaDefinitionPropertySet.contains(property)) {
				// Si la propriété n'est pas déclarée alors erreur
				if (undeclaredPropertySet == null) {
					undeclaredPropertySet = new HashSet<>();
				}
				undeclaredPropertySet.add(property);
			}
		}
		if (undeclaredPropertySet != null) {
			throw new IllegalStateException("Sur l'objet '" + getDefinitionKey().getName() + "' Il existe des propriétés non déclarées " + undeclaredPropertySet);
		}
	}

	private void checkMandatoryProperties(final Entity myEntity, final Set<EntityProperty> propertySet, final Set<EntityProperty> metaDefinitionPropertySet) {
		// Vérification des propriétés obligatoires
		Set<EntityProperty> unusedMandatoryPropertySet = null;
		for (final EntityProperty property : metaDefinitionPropertySet) {
			if (myEntity.isNotNull(property) && (!propertySet.contains(property) || getPropertyValue(property) == null)) {
				// Si la propriété obligatoire n'est pas renseignée alors erreur
				// Ou si la propriété obligatoire est renseignée mais qu'elle
				// est nulle alors erreur !
				if (unusedMandatoryPropertySet == null) {
					unusedMandatoryPropertySet = new HashSet<>();
				}
				unusedMandatoryPropertySet.add(property);
			}
		}
		if (unusedMandatoryPropertySet != null) {
			throw new IllegalStateException(getDefinitionKey().getName() + " Il existe des propriétés obligatoires non renseignées " + unusedMandatoryPropertySet);
		}
	}

}
