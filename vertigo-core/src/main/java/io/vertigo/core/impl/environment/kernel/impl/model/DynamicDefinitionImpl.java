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
package io.vertigo.core.impl.environment.kernel.impl.model;

import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.lang.Assertion;

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
	private final String dynamicDefinitionName;
	/**
	 * Conteneur des couples (propriétés, valeur)
	 */
	private final Map<String, Object> properties = new HashMap<>();
	/**
	 * Map des (FieldName, definitionKeyList)
	 */
	private final Map<String, List<String>> definitionNamesByFieldName = new LinkedHashMap<>();
	private final Map<String, List<DynamicDefinition>> definitionsByFieldName = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param dynamicDefinitionKey Clé de la définition
	 * @param entity Entité
	 */
	DynamicDefinitionImpl(final String dynamicDefinitionName, final Entity entity) {
		Assertion.checkNotNull(dynamicDefinitionName);
		//packageName peut être null
		Assertion.checkNotNull(entity);
		//-----
		this.dynamicDefinitionName = dynamicDefinitionName;
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
	public void check() {
		check(getEntity());
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinition build() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public final String getName() {
		return dynamicDefinitionName;
	}

	/** {@inheritDoc} */
	@Override
	public Object getPropertyValue(final String propertyName) {
		Assertion.checkNotNull(propertyName);
		// On ne vérifie rien sur le type retourné par le getter.
		// le type a été validé lors du put.
		//-----
		// Conformémément au contrat, on retourne null si pas de propriété
		// trouvée
		return properties.get(propertyName);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public final List<String> getDefinitionNames(final String fieldName) {
		return obtainDefinitionNames(fieldName);
	}

	/** {@inheritDoc} */
	@Override
	public final List<DynamicDefinition> getChildDefinitions(final String fieldName) {
		return obtainComposites(fieldName);
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
		properties.put(propertyName, value);
		return this;
	}

	private List<String> obtainDefinitionNames(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		List<String> list = definitionNamesByFieldName.get(fieldName);
		//-----
		if (list == null) {
			list = new ArrayList<>();
			definitionNamesByFieldName.put(fieldName, list);
		}
		return list;
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addChildDefinition(final String fieldName, final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		obtainComposites(fieldName).add(definition);
		return this;
	}

	private void doAddDefinition(final String fieldName, final String definitionName) {
		Assertion.checkNotNull(definitionName);
		//-----
		obtainDefinitionNames(fieldName).add(definitionName);
	}

	/** {@inheritDoc} */
	@Override
	public final DynamicDefinitionBuilder addDefinitions(final String fieldName, final List<String> definitionNames) {
		Assertion.checkNotNull(definitionNames);
		Assertion.checkArgument(obtainDefinitionNames(fieldName).isEmpty(), "syntaxe interdite : multi {0}", fieldName);
		//On vérifie que la liste est vide pour éviter les syntaxe avec multi déclarations
		//-----
		for (final String definitionName : definitionNames) {
			doAddDefinition(fieldName, definitionName);
		}
		return this;
	}

	private List<DynamicDefinition> obtainComposites(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		//-----
		List<DynamicDefinition> list = definitionsByFieldName.get(fieldName);
		//-----
		if (list == null) {
			list = new ArrayList<>();
			definitionsByFieldName.put(fieldName, list);
		}
		return list;
	}

	/** {@inheritDoc} */
	@Override
	//Cas des alter
			public final
			DynamicDefinitionBuilder addBody(final DynamicDefinition dynamicDefinition) {
		// 1. maj des EntityProperty
		for (final String propertyName : dynamicDefinition.getPropertyNames()) {
			addPropertyValue(propertyName, dynamicDefinition.getPropertyValue(propertyName));
		}

		// 2. maj fieldNameDefinitionKeyListMap
		final DynamicDefinitionImpl other = (DynamicDefinitionImpl) dynamicDefinition;

		for (final Entry<String, List<String>> entry : other.definitionNamesByFieldName.entrySet()) {
			obtainDefinitionNames(entry.getKey()).addAll(entry.getValue());
		}

		// 3.
		for (final Entry<String, List<DynamicDefinition>> entry : other.definitionsByFieldName.entrySet()) {
			obtainComposites(entry.getKey()).addAll(entry.getValue());
		}
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DynamicDefinitionBuilder addDefinition(final String fieldName, final String definitionName) {
		// On vérifie que la liste est vide pour éviter les syntaxe avec multi
		// déclarations
		Assertion.checkArgument(obtainDefinitionNames(fieldName).isEmpty(), "syntaxe interdite");
		//-----
		doAddDefinition(fieldName, definitionName);
		return this;
	}

	public void check(final Entity myEntity) {
		Assertion.checkNotNull(myEntity);
		//-----
		// 1.On vérifie la définition par rapport à la métadéfinition
		// 1.1 on vérifie les propriétés.
		final Set<String> propertyNames = getPropertyNames();
		final Set<String> metaDefinitionPropertyNames = myEntity.getPropertyNames();
		// 1.1.1 on vérifie que toutes les propriétés sont déclarées sur le
		// métamodèle
		checkProperties(propertyNames, metaDefinitionPropertyNames);

		// 1.1.2 on vérifie les propriétés obligatoires
		checkMandatoryProperties(myEntity, propertyNames, metaDefinitionPropertyNames);

		// 1.1.3 on vérifie les types des propriétés déclarées
		for (final String propertyName : propertyNames) {
			getEntity().getPrimitiveType(propertyName).checkValue(getPropertyValue(propertyName));
		}

		// 1.2 on vérifie les définitions composites (sous définitions).
		for (final DynamicDefinition dynamicDefinition : getAllChildDefinitions()) {
			dynamicDefinition.check();
		}

		// 1.3 on vérifie les définitions références.
		// TODO vérifier les définitions références
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		//nécessaire pour le log
		return dynamicDefinitionName;
	}

	private void checkProperties(final Set<String> propertyNames, final Set<String> metaDefinitionPropertyNames) {
		// Vérification que toutes les propriétés sont déclarées sur le
		// métamodèle
		final Set<String> undeclaredPropertyNames = new HashSet<>();
		for (final String propertyName : propertyNames) {
			if (!metaDefinitionPropertyNames.contains(propertyName)) {
				// Si la propriété n'est pas déclarée alors erreur
				undeclaredPropertyNames.add(propertyName);
			}
		}
		if (!undeclaredPropertyNames.isEmpty()) {
			throw new IllegalStateException("Sur l'objet '" + getName() + "' Il existe des propriétés non déclarées " + undeclaredPropertyNames);
		}
	}

	private void checkMandatoryProperties(final Entity myEntity, final Set<String> propertyNames, final Set<String> metaDefinitionPropertyNames) {
		// Vérification des propriétés obligatoires
		final Set<String> unusedMandatoryPropertySet = new HashSet<>();
		for (final String propertyName : metaDefinitionPropertyNames) {
			if (myEntity.isRequired(propertyName) && (!propertyNames.contains(propertyName) || getPropertyValue(propertyName) == null)) {
				// Si la propriété obligatoire n'est pas renseignée alors erreur
				// Ou si la propriété obligatoire est renseignée mais qu'elle
				// est nulle alors erreur !
				unusedMandatoryPropertySet.add(propertyName);
			}
		}
		if (!unusedMandatoryPropertySet.isEmpty()) {
			throw new IllegalStateException(getName() + " Il existe des propriétés obligatoires non renseignées " + unusedMandatoryPropertySet);
		}
	}
}
