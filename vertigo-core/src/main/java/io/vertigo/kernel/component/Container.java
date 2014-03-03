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
package io.vertigo.kernel.component;

import java.util.Set;

/**
 * Container universel des composants.
 * Les composants sont tous identifiés par un identifiant.
 * Cet identifiant est camelCase. (Voir REGEX ci-dessous)
 * 
 * @author pchretien, prahmoune
 */
public interface Container {
	//final Pattern REGEX_ID = Pattern.compile("[a-z][a-zA-Z0-9]{0,79}");

	/**
	 * Vérification si un composant est déjà enregistré.
	 * La casse du premier caractère de l'Id n'est pas prise en compte.
	 * @param id Id du composant
	 * @return Si le composant est déjà enregistré.
	 */
	boolean contains(final String id);

	/**
	 * Récupère un composant par son id et assure sn typage.
	 * La casse du premier caractère de l'Id n'est pas prise en compte.
	 * @param id Id du composant
	 * @return Composant
	 */
	<T> T resolve(final String id, final Class<T> componentClass);

	/**
	 * Liste des ids gérés.
	 * @return Liste des ids de TOUS les composants gérés par le présent container
	 */
	Set<String> keySet();
}
