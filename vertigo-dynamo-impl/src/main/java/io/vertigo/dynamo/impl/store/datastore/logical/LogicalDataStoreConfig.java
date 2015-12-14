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
package io.vertigo.dynamo.impl.store.datastore.logical;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.store.datastore.DataStorePlugin;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration logique des stores physiques.
 * @author pchretien, npiedeloup
 */
public final class LogicalDataStoreConfig {
	/** Map des stores utilisés spécifiquement */
	private final Map<String, DataStorePlugin> dataStorePluginsMap;

	/**
	 * @param dataStorePlugins DataStore plugins
	 */
	public LogicalDataStoreConfig(final List<DataStorePlugin> dataStorePlugins) {
		Assertion.checkNotNull(dataStorePlugins);
		//-----
		final Map<String, DataStorePlugin> pluginsMap = new HashMap<>();
		for (final DataStorePlugin dataStorePlugin : dataStorePlugins) {
			final String collection = dataStorePlugin.getCollection();
			final DataStorePlugin previous = pluginsMap.put(collection, dataStorePlugin);
			Assertion.checkState(previous == null, "DataStorePlugin {0}, was already registered", collection);
		}
		Assertion.checkNotNull(pluginsMap.get(DtDefinitionBuilder.DEFAULT_COLLECTION), "No " + DtDefinitionBuilder.DEFAULT_COLLECTION + " DataStorePlugin was set. Configure one and only one DataStorePlugin with name '" + DtDefinitionBuilder.DEFAULT_COLLECTION + "'.");
		dataStorePluginsMap = Collections.unmodifiableMap(pluginsMap);
	}

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param definition Définition
	 * @return Store utilisé pour cette definition
	 */
	public DataStorePlugin getPhysicalDataStore(final DtDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		return getDataStorePlugin(definition.getCollection());
	}

	/**
	 * Fournit le nom de la connection adpatée pour ce Store.
	 * @param storeName Nom du store
	 * @return Connection utilisée pour ce nom
	 */
	public String getConnectionName(final String storeName) {
		Assertion.checkArgNotEmpty(storeName);
		//-----
		return getDataStorePlugin(storeName).getConnectionName();
	}

	private DataStorePlugin getDataStorePlugin(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		final DataStorePlugin dataStore = dataStorePluginsMap.get(collection);
		Assertion.checkNotNull(dataStore, "No store found mapped to collection '{0}'", collection);
		return dataStore;
	}
}
