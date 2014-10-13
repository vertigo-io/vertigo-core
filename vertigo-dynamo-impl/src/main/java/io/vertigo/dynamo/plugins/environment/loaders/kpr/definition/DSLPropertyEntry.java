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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;

/**
 * Gestion des couples : propriété et valeur.
 *
 * @author pchretien
 */
public final class DSLPropertyEntry {
	private final String propertyValue;
	private final EntityProperty property;

	/**
	 * Constructeur.
	 * @param property Propriété
	 * @param propertyValue Valeur de la propriété
	 */
	public DSLPropertyEntry(final EntityProperty property, final String propertyValue) {
		Assertion.checkNotNull(property);
		//----------------------------------------------------------------------
		this.property = property;
		this.propertyValue = propertyValue;
	}

	/**
	 * @return Valeur de la propriété
	 */
	public String getPropertyValueAsString() {
		return propertyValue;
	}

	/**
	 * @return Propriété
	 */
	public EntityProperty getProperty() {
		return property;
	}
}
