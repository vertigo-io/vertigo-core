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
package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Une URI d'une DtListe représentant la liste complète des objets.
 * Elle est entièrement définie par
 *  - la dtDefinition de l'objet
 *
 * exemple :
 * - ALL_DT_PERSONNE.
 *
 * @author npiedeloup
 */
public final class DtListURIAll extends DtListURI {
	private static final long serialVersionUID = -1227046775032730925L;

	/**
	 * Constructeur.
	 * @param dtDefinition ID de la Définition de DT
	 */
	public DtListURIAll(final DtDefinition dtDefinition) {
		super(dtDefinition);
	}
}
