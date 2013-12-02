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
package io.vertigo.kernel.metamodel;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

/**
 * Utilitaire concernant les Definitions.
 *  
 * @author  pchretien 
 * @version $Id: DefinitionUtil.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class DefinitionUtil {
	private DefinitionUtil() {
		super();
	}

	public static String getPrefix(final Class<? extends Definition> definitionClass) {
		Assertion.checkNotNull(definitionClass);
		//---------------------------------------------------------------------
		final Prefix prefix = definitionClass.getAnnotation(Prefix.class);
		if (prefix == null) {
			throw new VRuntimeException("Annotation  '@Prefix' non trouv�e sur " + definitionClass.getName());
		}
		Assertion.checkArgNotEmpty(prefix.value());
		return prefix.value();
	}

	public static String getLocalName(final String name, final Class<? extends Definition> definitionClass) {
		//On enl�ve le prefix et le separateur.
		//On v�rifie aussi que le prefix est OK 
		final String prefix = getPrefix(definitionClass);
		Assertion.checkArgument(name.startsWith(prefix), "Le nom de la d�finition '{0}' ne commence pas par le prefix attendu : '{1}'", name, prefix);
		Assertion.checkArgument(name.charAt(prefix.length()) == Definition.SEPARATOR, "S�parateur utilis� pour la d�finition '{0}' n'est pas corerect", name);
		return name.substring(prefix.length() + 1);
	}
}
