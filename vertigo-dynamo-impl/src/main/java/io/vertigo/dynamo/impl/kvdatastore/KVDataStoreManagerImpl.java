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
package io.vertigo.dynamo.impl.kvdatastore;

import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public final class KVDataStoreManagerImpl implements KVDataStoreManager, Activeable {
	@Inject
	private List<KVDataStorePlugin> kvDataStorePlugins;
	private final Map<String, KVDataStorePlugin> kvDataStorePluginBinding = new HashMap<>();

	@Inject
	public KVDataStoreManagerImpl() {
		//nothing here
		//must wait starting time, for register kvDataStorePlugins
	}

	@Override
	public void start() {
		for(KVDataStorePlugin kvDataStorePlugin : kvDataStorePlugins) {
			registerKVDataStore(kvDataStorePlugin.getStoreName(), kvDataStorePlugin);
		}
	}

	@Override
	public void stop() {
		kvDataStorePluginBinding.clear();
	}

	public void put(String storeName,final String id, final Object objet) {
		getKVDataStorePlugin(storeName).put(id, objet);
	}

	public void remove(String storeName,final String id) {
		getKVDataStorePlugin(storeName).remove(id);
	}


	public <C> Option<C> find(String storeName,final String id, final Class<C> clazz) {
		return getKVDataStorePlugin(storeName).find(id, clazz);
	}

	public <C> List<C> findAll(String storeName,final int skip, final Integer limit, final Class<C> clazz) {
		return getKVDataStorePlugin(storeName).findAll(skip, limit, clazz);
	}
	
	private void registerKVDataStore(String storeName, KVDataStorePlugin kvDataStorePlugin) {
		Assertion.checkArgNotEmpty(storeName);
		Assertion.checkNotNull(kvDataStorePlugin);
		//---------------------------------------------------------------------
		kvDataStorePluginBinding.put(storeName, kvDataStorePlugin);
	}
	

	private KVDataStorePlugin getKVDataStorePlugin(String storeName) {
		KVDataStorePlugin kvDataStorePlugin = kvDataStorePluginBinding.get(storeName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(kvDataStorePlugin, "No KVDataStorePlugin bind to this name : {0}. Registered storeNames : ({1})", storeName, kvDataStorePluginBinding.keySet());
		//---------------------------------------------------------------------
		return kvDataStorePlugin;
	}

}
