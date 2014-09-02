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
package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.DataStorePlugin;

/**
 * Configuration logique des stores physiques.
 * @author pchretien
 */
public final class LogicalStoreConfiguration extends AbstractLogicalStoreConfiguration<DtDefinition, DataStorePlugin> {
	private final PersistenceManager persistenceManager;
	private final CollectionsManager collectionsManager;

	/**
	 * Constructeur.
	 * @param collectionsManager Manager des manipulations de liste
	 */
	public LogicalStoreConfiguration(final PersistenceManager persistenceManager, final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
		this.collectionsManager = collectionsManager;
	}

	PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	CollectionsManager getCollectionsManager() {
		return collectionsManager;
	}
}
