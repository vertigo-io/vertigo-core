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
package io.vertigo.core.dsl.entity;

import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.HashSet;
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
public final class Entity implements EntityType {
	/**
	 * Nom de la metadefinition (Type de la définition).
	 */
	private final String name;

	/**
	 * Map : Field by names
	 */
	private final Map<String, EntityField> fields;

	/**
	 * Constructeur de la MetaDefinition
	 * Une instance de MetaDefinition correspond à une classe -ou une interface- de Definition
	 * (Exemple : Classe Service).
	 * @param name Classe représentant l'instance métaDéfinition
	 */
	Entity(final String name, final Set<EntityField> fields) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(fields);
		//-----
		this.name = name;
		this.fields = new HashMap<>();

		for (final EntityField field : fields) {
			Assertion.checkArgument(!fields.contains(field.getName()), "field {0} is already registerd for {1}", field, this);
			//Une propriété est unique pour une définition donnée.
			//Il n'y a jamais de multiplicité
			this.fields.put(field.getName(), field);
		}
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
		final Set<String> names = new HashSet<>();
		for (final EntityField field : fields.values()) {
			if (field.getType().isPrimitive()) {
				names.add(field.getName());
			}
		}
		return names;
	}

	public EntityPropertyType getPrimitiveType(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkArgument(fields.containsKey(fieldName), "property {0} not found on {1}", fieldName, this);
		//-----
		return (EntityPropertyType) fields.get(fieldName).getType();
	}

	/**
	 * @param property Propriété
	 * @return Si la propriété mentionnée est nulle
	 */
	public boolean isRequired(final String fieldName) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkArgument(fields.containsKey(fieldName), "la propriete {0} n'est pas declaree pour {1}", fieldName, this);
		//-----
		return fields.get(fieldName).isRequired();
	}

	/**
	 * @return Set des attributs de l'entité
	 */
	public Set<EntityField> getAttributes() {
		final Set<EntityField> attributes = new HashSet<>();
		for (final EntityField field : fields.values()) {
			if (!field.getType().isPrimitive()) {
				attributes.add(field);
			}
		}
		return attributes;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}
}
