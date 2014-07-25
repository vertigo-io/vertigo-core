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
package io.vertigo.studio.impl.mda;

import io.vertigo.kernel.lang.Assertion;

import java.util.Properties;

/**
 * Helper de lecture d'un fichier de propriétés.
 * 
 * @author dchallas 
 */
public final class PropertiesUtil {

	private PropertiesUtil() {
		// rien
	}

	/**
	 * Retourne une propriété non null.
	 * @param properties Propriétés
	 * @param propertyName Nom de la propriété recherchée
	 * @param messageIfNull Message en cas de propriété non trouvée
	 * @return Valeur de la propriété
	 */
	public static String getPropertyNotNull(final Properties properties, final String propertyName, final String messageIfNull) {
		Assertion.checkNotNull(properties);
		Assertion.checkNotNull(propertyName);
		//---------------------------------------------------------------------
		final String property = properties.getProperty(propertyName, null);
		Assertion.checkNotNull(property, messageIfNull);
		return property.trim();
	}
}
