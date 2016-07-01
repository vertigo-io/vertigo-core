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
package io.vertigo.dynamo.domain.metamodel;

/**
 * Permet d'accéder aux données d'un objet de façon dynamique.
 * @author  pchretien
 */
public interface Dynamic {
	/**
	 * @return Définition de la resource.
	 */
	DtDefinition getDefinition();

	/**
	* Setter Générique.
	* Garantit que la valeur passée est conforme
	*  - au type enregistré pour le champ
	*  - les contraintes ne sont pas vérifiées.
	* @param dtField Field
	* @param value Object
	*/
	void setValue(final DtField dtField, final Object value);

	/**
	 * Getter générique.
	 * Garantit que la valeur retournée est conforme
	 *  - au type enregistré pour le champ
	 *
	 * @param dtField Field
	 * @return valeur non typée
	 */
	Object getValue(final DtField dtField);
}
