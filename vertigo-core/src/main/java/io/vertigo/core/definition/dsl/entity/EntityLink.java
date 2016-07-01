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

import io.vertigo.lang.Assertion;

/**
 * Defines a link to an entity.
 * @author pchretien
 */
public final class EntityLink implements EntityType {
	private final Entity entity;

	/**
	 * Constructor
	 * @param entity the entity that is linked
	 */
	EntityLink(final Entity entity) {
		Assertion.checkNotNull(entity);
		Assertion.checkState(!entity.isPrimitive(), "A primitive entity such as {0} can't be linked", entity);
		//-----
		this.entity = entity;
	}

	/**
	 * @return the linked entity
	 */
	public Entity getEntity() {
		return entity;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}
}
