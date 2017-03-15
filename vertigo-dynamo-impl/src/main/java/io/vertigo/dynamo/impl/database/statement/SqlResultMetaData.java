/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.database.statement;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;

/**
 * Implémentation par défaut de StatementHandler.
 * Toute l'information est portée par le domain.
 *
 * @author  pchretien
 */
final class SqlResultMetaData {
	private final DtDefinition dtDefinition;
	private final boolean isDtObject;

	/**
	 * Constructeur.
	 */
	SqlResultMetaData(final DtDefinition dtDefinition, final boolean isDtObject) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.dtDefinition = dtDefinition;
		this.isDtObject = isDtObject;
	}

	DtObject createDtObject() {
		return DtObjectUtil.createDtObject(dtDefinition);
	}

	/***
	 * @return Si le type de sortie est un DTO.
	 */
	boolean isDtObject() {
		return isDtObject;
	}

	/***
	 * Récupération de la DtDefinition du type de retour du PrepareStatement.
	 * @return DtDefinition du type de retour du PrepareStatement
	 */
	DtDefinition getDtDefinition() {
		return dtDefinition;
	}
}
