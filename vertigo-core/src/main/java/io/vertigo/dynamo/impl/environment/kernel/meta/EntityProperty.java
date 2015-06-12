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

import io.vertigo.lang.Assertion;

/**
 * Propriété (meta-data, aspect, attribute) d'une entity.
 *
 * @author  pchretien, npiedeloup
 */
public final class EntityProperty {
	private final String name;
	private final EntityPropertyType primitiveType;

	public EntityProperty(final String name, final EntityPropertyType primitiveType) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(primitiveType);
		//-----
		this.name = name;
		this.primitiveType = primitiveType;
	}

	/**
	 * @return Nom de la propriété (Const)
	 */
	public String getName() {
		return name;
	}

	/**
	 * Toute propriété dynamo est déclarée dans un type primitif .
	 * Ceci permet de gérer au mieux l'utilisation des propriétés dans la grammaire.
	 * @return Type primitif utilisé pour déclarer la valuer de la propriété.
	 */
	public EntityPropertyType getPrimitiveType() {
		return primitiveType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
