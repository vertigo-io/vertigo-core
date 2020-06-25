/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.definition;

import io.vertigo.core.lang.Assertion;

/**
 * This class provides usefull Utilitaire concernant les Definitions.
 *
 * @author  pchretien
 */
public final class DefinitionUtil {
	private DefinitionUtil() {
		super();
	}

	/**
	 * Return the prefix concerning the type of the defintion.
	 * @param definitionClass Type of the definition
	 * @return the prefix concerning the type of the defintion
	 */
	public static String getPrefix(final Class<? extends Definition> definitionClass) {
		Assertion.check()
				.isNotNull(definitionClass);
		//-----
		final DefinitionPrefix prefix = definitionClass.getAnnotation(DefinitionPrefix.class);
		//-----
		Assertion.check()
				.isNotNull(prefix, "Annotation '@DefinitionPrefix' not found on {0}", definitionClass.getName())
				.isNotBlank(prefix.value());
		return prefix.value();
	}

	/**
	 * Returns the short name of the definition.
	 * @param definitionName Name of the definition
	 * @param definitionClass Type of the definition
	 * @return the short name of the definition
	 */
	public static String getLocalName(final String definitionName, final Class<? extends Definition> definitionClass) {
		Assertion.check()
				.isNotBlank(definitionName)
				.isNotNull(definitionClass);
		//-----
		//On enléve le prefix et le separateur.
		//On vérifie aussi que le prefix est OK
		final String prefix = getPrefix(definitionClass);
		Assertion.check()
				.isTrue(definitionName.startsWith(prefix), "Le nom de la définition '{0}' ne commence pas par le prefix attendu : '{1}'", definitionName, prefix);
		return definitionName.substring(prefix.length());
	}

	/**
	 * Checks if the name of a definition is valid for the specified type.
	 * If not an exception is thrown.
	 * @param definitionName Name of the definition
	 * @param definitionClass Type of the definition
	 */
	public static void checkName(final String definitionName, final Class<? extends Definition> definitionClass) {
		Assertion.check()
				.isNotBlank(definitionName)
				.isNotNull(definitionClass);
		//-----
		final String prefix = DefinitionUtil.getPrefix(definitionClass);
		Assertion.check()
				.isTrue(definitionName.startsWith(prefix), "La définition {0} doit commencer par {1}", definitionName, prefix)
				.isTrue(definitionName.length() > prefix.length(), "Le nom de la définition doit être renseigné")
				.isTrue(Character.isUpperCase(definitionName.charAt(prefix.length())), "the name of the dtDefinition {0} must be in UpperCamelCase", definitionName)
				.isTrue(Definition.REGEX_DEFINITION_NAME.matcher(definitionName).matches(), "urn de définition {0} doit matcher le pattern {1}", definitionName, Definition.REGEX_DEFINITION_NAME);
	}

}
