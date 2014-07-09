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
package io.vertigo.dynamock.dao.famille;

import io.vertigo.dynamo.impl.persistence.util.DAOBroker;
import io.vertigo.dynamo.persistence.PersistenceManager;

import javax.inject.Inject;

/**
 * DAO : accès à un object (DTO, DTC). 
 * FamilleDAO
 */
public final class FamilleDAO extends DAOBroker<io.vertigo.dynamock.domain.famille.Famille, java.lang.Long> {

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 */
	@Inject
	public FamilleDAO(final PersistenceManager persistenceManager) {
		super(io.vertigo.dynamock.domain.famille.Famille.class, persistenceManager);
	}
}
