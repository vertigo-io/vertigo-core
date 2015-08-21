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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Une entité permet de décrire un modèle, une classe.
 * - Elle est définie par son nom.
 * - Elle possède une liste de propriétés (Chacune étant obligatoire / facultative)
 * - Elle est composée d'une liste d'attibuts.
 *
 * Une entité permet, ainsi, d'adopter des comportement dynamique, de fabriquer des grammaires.
 * Si l'ensemble des définitions permet de construire le modèle, l'ensemble des entités permet de décrire le métamodèle.
 *
 * @author pchretien
 */
public final class Entity {
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
	private final Map<String, EntityProperty> properties;

	/**
	 * Constructeur de la MetaDefinition
	 * Une instance de MetaDefinition correspond à une classe -ou une interface- de Definition
	 * (Exemple : Classe Service).
	 * @param name Classe représentant l'instance métaDéfinition
	 */
	Entity(final String name, final Set<EntityAttribute> attributes, final Set<EntityProperty> properties) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(attributes);
		Assertion.checkNotNull(properties);
		//-----
		this.name = name;
		this.attributes = Collections.unmodifiableSet(attributes);

		final Map<String, EntityProperty> map = new HashMap<>();
		for (final EntityProperty entityProperty : properties) {
			Assertion.checkArgument(!map.containsKey(entityProperty.getName()), "property {0} is already registerd for {1}", entityProperty, this);
			//Une propriété est unique pour une définition donnée.
			//Il n'y a jamais de multiplicité
			map.put(entityProperty.getName(), entityProperty);
		}
		this.properties = Collections.unmodifiableMap(map);
	}

	/**
	 * @return Nom de l'entité (Type de la définition).
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Ensemble de toutes les propriétés gérées (obligatoires ou non).
	 */
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	public EntityPropertyType getPrimitiveType(final String propertyName) {
		Assertion.checkNotNull(propertyName);
		Assertion.checkArgument(properties.containsKey(propertyName), "property {0} not found on {1}", propertyName, this);
		//-----
		return properties.get(propertyName).getPrimitiveType();
	}

	/**
	 * @param property Propriété
	 * @return Si la propriété mentionnée est nulle
	 */
	public boolean isRequired(final String propertyName) {
		Assertion.checkNotNull(propertyName);
		Assertion.checkArgument(properties.containsKey(propertyName), "la propriete {0} n'est pas declaree pour {1}", propertyName, this);
		//-----
		return properties.get(propertyName).isRequired();
	}

	/**
	 * @return Set des attributs de l'entité
	 */
	public Set<EntityAttribute> getAttributes() {
		return attributes;
	}
}
