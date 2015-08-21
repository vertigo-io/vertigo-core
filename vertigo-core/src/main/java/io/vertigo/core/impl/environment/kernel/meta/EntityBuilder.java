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
package io.vertigo.core.impl.environment.kernel.meta;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.HashSet;
import java.util.Set;

/**
 * Builder des entities.
 *
 * @author pchretien
 */
public final class EntityBuilder implements Builder<Entity> {
	/**
	 * Nom de la metadefinition (Type de la définition).
	 */
	private final String name;

	/**
	 * Liste de TOUTES les définitions (composites et références) acceptées.
	 */
	private final Set<EntityAttribute> attributes;
	/**
	 * Map permettant de savoir si une propriété est obligatoire, facultative (Property, Boolean)
	 * Set des propriétés autorisées pour la définition
	 * est représenté par la liste des clés de la Map.
	 */
	private final Set<EntityProperty> properties;

	/**
	 * Constructeur de la MetaDefinition
	 * Une instance de MetaDefinition correspond à une classe -ou une interface- de Definition
	 * (Exemple : Classe Service).
	 * @param name Classe représentant l'instance métaDéfinition
	 */
	public EntityBuilder(final String name) {
		Assertion.checkNotNull(name);
		//-----
		this.name = name;
		attributes = new HashSet<>();
		properties = new HashSet<>();

	}

	/**
	 * Ajout d'un attribut.
	 * @param fieldName Nom
	 * @param entity Entité référencée
	 * @param notNull Si l'attribut est obligatoire
	 */
	public EntityBuilder addAttribute(final String fieldName, final Entity entity, final boolean notNull) {
		return addAttribute(fieldName, entity, false, notNull);
	}

	/**
	 * Ajout d'un attribut multiple.
	 * @param fieldName Nom
	 * @param entity Entité référencée
	 * @param notNull Si l'attribut est obligatoire
	 */
	public EntityBuilder addAttributes(final String fieldName, final Entity entity, final boolean notNull) {
		return addAttribute(fieldName, entity, true, notNull);
	}

	/**
	 * Ajout d'un attribut.
	 * @param fieldName Nom
	 * @param entity Entité référencée
	 * @param multiple Si il y a plusieurs entités référencées
	 * @param required Si l'attribut est obligatoire
	 */
	private EntityBuilder addAttribute(final String fieldName, final Entity entity, final boolean multiple, final boolean required) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(entity);
		//On vérifie que le nom du champ n'est pas déjà utilisé.
		//-----
		final EntityAttribute entityAttribute = new EntityAttribute(fieldName, entity, multiple, required);
		//-----
		attributes.add(entityAttribute);
		return this;
	}

	/**
	 * Ajout d'une propriété.
	 * @param property Propriété
	 * @param notNull Si la propriété est obligatoire
	 */
	public EntityBuilder addProperty(final String propertyName, final EntityPropertyType type, final boolean required) {
		properties.add(new EntityProperty(propertyName, type, required));
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Entity build() {
		return new Entity(name, attributes, properties);
	}
}
