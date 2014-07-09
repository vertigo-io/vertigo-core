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
package io.vertigo.commons.parser;

/**
 * Règle.
 * Si elle est respectée l'index augmente sinon une erreur est déclenchée.
 *
 * @author pchretien
 */
public interface Parser<P> {
	/**
	 * Retourne le prochain numéro de ligne
	 * Le pattern est OK du numéro de ligne passé en paramètre au numéro de ligne retourné.
	 * @param text Texte à parser
	 * @param start Début du parsing
	 * @throws NotFoundException Si la règle n'est pas applicable.
	 * @return Index de fin
	 */
	int parse(String text, int start) throws NotFoundException;

	P get();
}
