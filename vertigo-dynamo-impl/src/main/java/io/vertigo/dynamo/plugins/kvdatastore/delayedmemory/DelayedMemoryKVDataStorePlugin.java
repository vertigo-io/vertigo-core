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
package io.vertigo.dynamo.plugins.kvdatastore.delayedmemory;

import io.vertigo.dynamo.impl.store.kvstore.KVDataStorePlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

/**
 * Memory implementation of UiSecurityTokenCachePlugin.
 * Purge is garantee Timer et passe toutes les minutes.
 *
 * @author pchretien, npiedeloup
 */
public final class DelayedMemoryKVDataStorePlugin implements KVDataStorePlugin, Activeable {

	private static final Logger LOGGER = Logger.getLogger(DelayedMemoryKVDataStorePlugin.class);

	private final String dataStoreName;
	private Timer purgeTimer;
	private final long timeToLiveSeconds;
	private final DelayQueue<DelayedMemoryKey> timeoutQueue = new DelayQueue<>();
	private final Map<String, DelayedMemoryCacheValue> cacheDatas = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * @param dataStoreName Store utilisé
	 * @param timeToLiveSeconds life time of elements (seconde)
	 */
	@Inject
	public DelayedMemoryKVDataStorePlugin(final @Named("dataStoreName") String dataStoreName, final @Named("timeToLiveSeconds") int timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(dataStoreName);
		//-----
		this.dataStoreName = dataStoreName;
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public String getDataStoreName() {
		return dataStoreName;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String key, final Object data) {
		Assertion.checkNotNull(data);
		//-----
		final DelayedMemoryCacheValue cacheValue = new DelayedMemoryCacheValue(data);
		cacheDatas.put(key, cacheValue);
		timeoutQueue.put(new DelayedMemoryKey(key, cacheValue.getCreateTime() + timeToLiveSeconds * 1000));
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String key) {
		cacheDatas.remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public <C> Option<C> find(final String key, final Class<C> clazz) {
		final DelayedMemoryCacheValue cacheValue = cacheDatas.get(key);
		if (cacheValue != null && !isTooOld(cacheValue)) {
			return Option.some(clazz.cast(cacheValue.getValue()));
		}
		cacheDatas.remove(key);
		return Option.none(); //key expired : return null
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final int skip, final Integer limit, final Class<C> clazz) {
		throw new UnsupportedOperationException("This implementation doesn't use ordered datas. Method findAll can't be called.");
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final long purgePeriod = 1 * 60 * 1000;
		purgeTimer = new Timer("PurgeMemoryUiSecurityTokenCache", true);
		purgeTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				removeTooOldElements();
			}
		}, purgePeriod, purgePeriod);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		purgeTimer.cancel();
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
}
