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
package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Une grammaire est composée d'entités et de propriétés.
 * Les entités sont une composition d'entités et de propriétés.
 * 
 * Il est possible de composer une grammaire à partir de grammaires.
 *
 * @author pchretien
 */
public final class Grammar {
	private final Map<String, Entity> entities = new HashMap<>();
	private final Map<String, EntityProperty> properties = new HashMap<>();

	/**
	 * Ajout d'une grammaire.
	 * @param grammars Grammaires à ajouter
	 */
	public Grammar(final List<Grammar> grammars) {
		for (final Grammar grammar : grammars) {
			for (final Entity entity : grammar.getEntities()) {
				putEntity(entity);
			}
			for (final EntityProperty property : grammar.getProperties()) {
				putProperty(property);
			}
		}
	}

	/**
	 * Ajout d'une entité à la grammaire.
	 * @param entity Entité
	 * @return Entité présente avec le même nom (Null si aucune)
	 */
	private Entity putEntity(final Entity entity) {
		Assertion.checkNotNull(entity);
		//---------------------------------------------------------------------
		return entities.put(entity.getName(), entity);
	}

	/**
	 * Ajout d'une propriété à la grammaire.
	 * @param property Propriété
	 * @return Propriété présente avec le même nom (Null si aucune)
	 */
	private EntityProperty putProperty(final EntityProperty property) {
		Assertion.checkNotNull(property);
		//---------------------------------------------------------------------
		final String propertyName = StringUtil.constToCamelCase(property.getName(), false);
		return properties.put(propertyName, property);
	}

	/**
	 * Récupération d'une propriété par son nom.
	 * @param propertyName Nom de la propriété
	 * @return Propriété trouvée (NotNull)
	 */
	public EntityProperty getProperty(final String propertyName) {
		final EntityProperty property = properties.get(propertyName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(property, "propriété non trouvée '{0}' parmi : {1}", propertyName, properties);
		return property;
	}

	/**
	 * @return Entité.
	 */
	public Entity getEntity(final String name) {
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		final Entity entity = entities.get(name);
		Assertion.checkNotNull(entity, "entity {0} not found", name);
		return entity;
	}

	/**
	 * @return Liste des entités.
	 */
	public List<Entity> getEntities() {
		return Collections.unmodifiableList(new ArrayList<>(entities.values()));
	}

	/**
	 * @return Liste des propriétés connues de la grammaire.
	 */
	public List<EntityProperty> getProperties() {
		return Collections.unmodifiableList(new ArrayList<>(properties.values()));
	}

	/**
	 * Enregistre une nouvelle entité.
	 * @param entity Type de la définition
	 */
	public void registerEntity(final Entity entity) {
		final Entity previous = putEntity(entity);
		//---------------------------------------------------------------------
		Assertion.checkArgument(previous == null, "Entité {0} deja enregistrée", entity);
		//---------------------------------------------------------------------
		for (final EntityProperty property : entity.getProperties()) {
			putProperty(property);
		}
	}
}
