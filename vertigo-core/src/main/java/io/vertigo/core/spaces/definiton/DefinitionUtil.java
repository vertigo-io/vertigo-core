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
package io.vertigo.core.spaces.definiton;

import io.vertigo.lang.Assertion;

/**
 * Utilitaire concernant les Definitions.
 *
 * @author  pchretien
 */
public final class DefinitionUtil {
	private DefinitionUtil() {
		super();
	}

	public static String getPrefix(final Class<? extends Definition> definitionClass) {
		Assertion.checkNotNull(definitionClass);
		//-----
		final DefinitionPrefix prefix = definitionClass.getAnnotation(DefinitionPrefix.class);
		if (prefix == null) {
			throw new RuntimeException("Annotation '@DefinitionPrefix' not found on " + definitionClass.getName());
		}
		Assertion.checkArgNotEmpty(prefix.value());
		return prefix.value();
	}

	public static String getLocalName(final String name, final Class<? extends Definition> definitionClass) {
		//On enléve le prefix et le separateur.
		//On vérifie aussi que le prefix est OK
		final String prefix = getPrefix(definitionClass);
		Assertion.checkArgument(name.startsWith(prefix), "Le nom de la définition '{0}' ne commence pas par le prefix attendu : '{1}'", name, prefix);
		Assertion.checkArgument(name.charAt(prefix.length()) == Definition.SEPARATOR, "Séparateur utilisé pour la définition '{0}' n'est pas correct", name);
		return name.substring(prefix.length() + 1);
	}
}
