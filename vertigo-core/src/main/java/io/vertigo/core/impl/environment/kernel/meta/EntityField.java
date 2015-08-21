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

/**
 * Attribut d'une entité.
 *
 * @author pchretien
 */
public final class EntityField {
	private final String name;
	private final boolean multiple;
	private final boolean required;
	private final EntityType type;

	/**
	 * Constructeur.
	 * @param name Nom
	 * @param entity Entité / Méta-définition parente (composition ou référence)
	 * @param multiple Si multiple
	 * @param required Si not null
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
	 * @return Nom
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Si multiple
	 */
	boolean isMultiple() {
		return multiple;
	}

	/**
	 * @return Si not null
	 */
	boolean isRequired() {
		return required;
	}

	/**
	 * @return Entité référencée. (composition ou référence)
	 */
	public EntityType getType() {
		return type;
	}
}
