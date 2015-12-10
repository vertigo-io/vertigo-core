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
package io.vertigo.core.definition.dsl.entity;

import io.vertigo.lang.Assertion;

/**
 * Field of an entity.
 *
 * @author pchretien
 */
public final class EntityField {
	private final String name;
	private final boolean multiple;
	private final boolean required;
	private final EntityType type;

	/**
	 * Constructor.
	 * @param name Name
	 * @param type Type of the entity
	 * @param multiple If multiple
	 * @param required If not null
	 */
	EntityField(final String name, final EntityType type, final boolean multiple, final boolean required) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(type);
		//-----
		this.name = name;
		this.multiple = multiple;
		this.required = required;
		this.type = type;
	}

	/**
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return If multiple
	 */
	boolean isMultiple() {
		return multiple;
	}

	/**
	 * @return If required
	 */
	boolean isRequired() {
		return required;
	}

	/**
	 * @return the type of the entity
	 */
	public EntityType getType() {
		return type;
	}
}
