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
 * This class defines how the collections are mapped to the physical stores.
 * 
 * @author pchretien, npiedeloup
 */
public final class LogicalDataStoreConfig {
	/**
	 * Map (collection-dataStorePlugin). 
	 * This map defines the dataStore for each collection */
	private final Map<String, DataStorePlugin> dataStorePluginsMap;

	/**
	 * Constructor.
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
	 * Provides a 'DataStorePlugin' for the specified 'DtDefinition'.
	 * Each DtDefinition is mapped to a collection.
	 * @param dtDefinition the DtDefinition 
	 * @return the dataStore used for the specified 'DtDefinition'
	 */
	public DataStorePlugin getPhysicalDataStore(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		return getDataStorePlugin(dtDefinition.getCollection());
	}

	/**
	 * Provides the name of the connection.
	 * @param collection the collection
	 * @return the name of the connection
	 */
	public String getConnectionName(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		return getDataStorePlugin(collection).getConnectionName();
	}

	private DataStorePlugin getDataStorePlugin(final String collection) {
		Assertion.checkArgNotEmpty(collection);
		//-----
		final DataStorePlugin dataStore = dataStorePluginsMap.get(collection);
		Assertion.checkNotNull(dataStore, "No store found mapped to collection '{0}'", collection);
		return dataStore;
	}
}
