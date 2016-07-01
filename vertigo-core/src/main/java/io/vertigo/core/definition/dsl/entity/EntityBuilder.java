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
package io.vertigo.core.definition.dsl.entity;

import java.util.HashSet;
import java.util.Set;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This class provides a common way to build an entity.
 *
 * @author pchretien
 */
public final class EntityBuilder implements Builder<Entity> {
	/**
	 * Name of the entity.
	 */
	private final String name;

	/**
	 * Fields of the entity.
	 */
	private final Set<EntityField> fields;

	private boolean myRoot;

	/**
	 * Constructor.
	 * @param name the name of the entity
	 */
	public EntityBuilder(final String name) {
		Assertion.checkNotNull(name);
		//-----
		this.name = name;
		fields = new HashSet<>();

	}

	public EntityBuilder withRoot() {
		myRoot = true;
		return this;
	}

	/**
	 * Adds a new simple field.
	 * @param fieldName Name of the field
	 * @param type Type of the field
	 * @param required If the field is required
	 * @return this builder
	 */
	public EntityBuilder addField(final String fieldName, final EntityType type, final boolean required) {
		return addField(fieldName, type, false, required);
	}

	/**
	 * Adds a new multi field defined by an entity.
	 * @param fieldName Name of the field
	 * @param entity Type of the field
	 * @param required If the field is required
	 * @return this builder
	 */
	public EntityBuilder addFields(final String fieldName, final Entity entity, final boolean required) {
		//Only Entities may be multiple
		return addField(fieldName, entity, true, required);
	}

	/**
	 * Adds a new multi field defined by an entity.
	 * @param fieldName Name of the field
	 * @param entityLink Type of the field
	 * @param required If the field is required
	 * @return this builder
	 */
	public EntityBuilder addFields(final String fieldName, final EntityLink entityLink, final boolean required) {
		//Only Entities or  Link may be multiple
		return addField(fieldName, entityLink, true, required);
	}

	/**
	 * Adds a new field.
	 * @param fieldName Name of the field
	 * @param type Type of the field
	 * @param multiple If the field can have many values
	 * @param required If the field is required
	 * @return this builder
	 */
	private EntityBuilder addField(final String fieldName, final EntityType type, final boolean multiple, final boolean required) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(type);
		//-----
		final EntityField field = new EntityField(fieldName, type, multiple, required);
		//-----
		fields.add(field);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public Entity build() {
		return new Entity(name, fields, myRoot);
	}
}
