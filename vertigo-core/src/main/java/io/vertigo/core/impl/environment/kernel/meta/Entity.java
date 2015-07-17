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
	private final Map<EntityProperty, Boolean> properties;

	/**
	 * Constructeur de la MetaDefinition
	 * Une instance de MetaDefinition correspond à une classe -ou une interface- de Definition
	 * (Exemple : Classe Service).
	 * @param name Classe représentant l'instance métaDéfinition
	 */
	Entity(final String name, final Set<EntityAttribute> attributes, final Map<EntityProperty, Boolean> properties) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(attributes);
		Assertion.checkNotNull(properties);
		//-----
		this.name = name;
		this.attributes = Collections.unmodifiableSet(attributes);
		this.properties = Collections.unmodifiableMap(properties);
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
	public Set<EntityProperty> getProperties() {
		return properties.keySet();
	}

	/**
	 * @param property Propriété
	 * @return Si la propriété mentionnée est nulle
	 */
	public boolean isNotNull(final EntityProperty property) {
		Assertion.checkNotNull(property);
		Assertion.checkArgument(properties.containsKey(property), "la propriete {0} n'est pas declaree pour {1}", property, this);
		//-----
		return properties.get(property);
	}

	/**
	 * @return Set des attributs de l'entité
	 */
	public Set<EntityAttribute> getAttributes() {
		return attributes;
	}
}
