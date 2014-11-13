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
package io.vertigo.dynamo.impl.persistence;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.impl.persistence.util.BrokerNNImpl;
import io.vertigo.dynamo.persistence.Broker;
import io.vertigo.dynamo.persistence.BrokerConfiguration;
import io.vertigo.dynamo.persistence.BrokerNN;
import io.vertigo.dynamo.persistence.DataStorePlugin;
import io.vertigo.dynamo.persistence.MasterDataConfiguration;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import javax.inject.Inject;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class PersistenceManagerImpl implements PersistenceManager {
	private final MasterDataConfiguration masterDataConfiguration;
	private final BrokerConfigurationImpl brokerConfiguration;
	/** Broker des objets métier et des listes. */
	private final Broker broker;
	private final BrokerNN brokerNN;

	/**
	 * Constructeur.
	 * @param cacheManager Manager de gestion du cache
	 * @param collectionsManager Manager de gestion des collections
	 */
	@Inject
	public PersistenceManagerImpl(final TaskManager taskManager, final CacheManager cacheManager, final CollectionsManager collectionsManager, final Option<FileStorePlugin> fileStorePlugin, final DataStorePlugin defaultStorePlugin) {
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(cacheManager);
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(fileStorePlugin);
		Assertion.checkNotNull(defaultStorePlugin);
		//---------------------------------------------------------------------
		masterDataConfiguration = new MasterDataConfigurationImpl(collectionsManager);
		brokerConfiguration = new BrokerConfigurationImpl(cacheManager, this, collectionsManager);
		brokerNN = new BrokerNNImpl(taskManager);
		//---
		//On enregistre le plugin de gestion des fichiers : facultatif
		if (fileStorePlugin.isDefined()) {
			brokerConfiguration.getLogicalFileStoreConfiguration().registerDefaultPhysicalStore(fileStorePlugin.get());
		}
		//On enregistre le plugin principal du broker : DefaultPhysicalStore
		brokerConfiguration.getLogicalStoreConfiguration().registerDefaultPhysicalStore(defaultStorePlugin);
		broker = new BrokerImpl(brokerConfiguration);
	}

	/** {@inheritDoc} */
	@Override
	public MasterDataConfiguration getMasterDataConfiguration() {
		return masterDataConfiguration;
	}

	//	/**
	//	 * @param storePluginId Id du plugin
	//	 * @param dtDefinition DtDefinition
	//	 */
	//	public void definePhysicalStore(final String storePluginId, final DtDefinition dtDefinition) {
	//		final StorePlugin storePlugin = Home.getContainer().getPlugin(storePluginId, StorePlugin.class, PersistenceManager.class);
	//		brokerConfiguration.getLogicalStoreConfiguration().register(dtDefinition, storePlugin);
	//	}

	/**
	 * @return Configuration du PersistenceManager
	 */
	@Override
	public BrokerConfiguration getBrokerConfiguration() {
		return brokerConfiguration;
	}

	//-------------------------------------------------------------------------

	/** {@inheritDoc} */
	@Override
	public Broker getBroker() {
		return broker;
	}

	@Override
	public BrokerNN getBrokerNN() {
		return brokerNN;
	}
}
