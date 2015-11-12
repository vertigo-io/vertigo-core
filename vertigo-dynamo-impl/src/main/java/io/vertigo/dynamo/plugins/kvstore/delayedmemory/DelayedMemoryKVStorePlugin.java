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
package io.vertigo.dynamo.plugins.kvstore.delayedmemory;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.dynamo.impl.kvstore.KVStorePlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.ListBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

/**
 * Memory implementation of UiSecurityTokenCachePlugin.
 * This store ISN'T transactional !!
 * Purge is garantee Timer et passe toutes les minutes.
 *
 * @author pchretien, npiedeloup
 */
public final class DelayedMemoryKVStorePlugin implements KVStorePlugin {

	private static final Logger LOGGER = Logger.getLogger(DelayedMemoryKVStorePlugin.class);

	private final String dataStoreName;
	private final List<String> collections;

	private final long timeToLiveSeconds;
	private final DelayQueue<DelayedMemoryKey> timeoutQueue = new DelayQueue<>();
	private final Map<String, DelayedMemoryCacheValue> cacheDatas = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * @param daemonManager Manager des daemons
	 * @param dataStoreName Store utilisé
	 * @param timeToLiveSeconds life time of elements (seconde)
	 */
	@Inject
	public DelayedMemoryKVStorePlugin(
			final @Named("dataStoreName") String dataStoreName,
			final @Named("collections") String collections,
			final DaemonManager daemonManager,
			final @Named("timeToLiveSeconds") int timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(dataStoreName);
		Assertion.checkArgNotEmpty(collections);
		//-----
		this.dataStoreName = dataStoreName;
		final ListBuilder<String> listBuilder = new ListBuilder<>();
		for (final String collection : collections.split(", ")) {
			listBuilder.add(collection.trim());
		}
		this.collections = listBuilder.unmodifiable().build();
		//-----
		this.timeToLiveSeconds = timeToLiveSeconds;

		final int purgePeriod = Math.min(1 * 60, timeToLiveSeconds);
		daemonManager.registerDaemon("kvDataStoreCache", RemoveTooOldElementsDaemon.class, purgePeriod, this);
	}

	/** {@inheritDoc} */
	@Override
	public String getDataStoreName() {
		return dataStoreName;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getCollections() {
		return collections;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String key, final Object data) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(data);
		//-----
		final DelayedMemoryCacheValue cacheValue = new DelayedMemoryCacheValue(data);
		cacheDatas.put(key, cacheValue);
		timeoutQueue.put(new DelayedMemoryKey(key, cacheValue.getCreateTime() + timeToLiveSeconds * 1000));
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String key) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		//-----
		cacheDatas.remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public <C> Option<C> find(final String collection, final String key, final Class<C> clazz) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(clazz);
		//-----
		final DelayedMemoryCacheValue cacheValue = cacheDatas.get(key);
		if (cacheValue != null && !isTooOld(cacheValue)) {
			return Option.some(clazz.cast(cacheValue.getValue()));
		}
		cacheDatas.remove(key);
		return Option.none(); //key expired : return null
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final String collection, final int skip, final Integer limit, final Class<C> clazz) {
		throw new UnsupportedOperationException("This implementation doesn't use ordered datas. Method findAll can't be called.");
	}

	/**
	 * Purge les elements trop vieux.
	 */
	void removeTooOldElements() {
		final int maxChecked = 500;
		int checked = 0;
		//Les elements sont parcouru dans l'ordre d'insertion (sans lock)
		while (checked < maxChecked) {
			final DelayedMemoryKey delayedKey = timeoutQueue.poll();
			if (delayedKey != null) {
				cacheDatas.remove(delayedKey.getKey());
				checked++;
			} else {
				break;
			}
		}
		LOGGER.info("purge " + checked + " elements");
	}

	private boolean isTooOld(final DelayedMemoryCacheValue cacheValue) {
		return System.currentTimeMillis() - cacheValue.getCreateTime() >= timeToLiveSeconds * 1000;
	}

	/**
	 *
	 * @author npiedeloup
	 */
	public static final class RemoveTooOldElementsDaemon implements Daemon {
		private final DelayedMemoryKVStorePlugin delayedMemoryKVDataStorePlugin;

		/**
		 * @param delayedMemoryKVDataStorePlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final DelayedMemoryKVStorePlugin delayedMemoryKVDataStorePlugin) {
			Assertion.checkNotNull(delayedMemoryKVDataStorePlugin);
			//------
			this.delayedMemoryKVDataStorePlugin = delayedMemoryKVDataStorePlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			delayedMemoryKVDataStorePlugin.removeTooOldElements();
		}
	}
}
