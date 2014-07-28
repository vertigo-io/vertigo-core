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
package io.vertigo.vega.plugins.rest.security.memory;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.vega.impl.security.UiSecurityTokenCachePlugin;

import java.io.Serializable;
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
public final class MemoryUiSecurityTokenCachePlugin implements Activeable, UiSecurityTokenCachePlugin {

	private final Logger logger = Logger.getLogger(getClass());

	private Timer purgeTimer;
	private final long timeToLiveSeconds;
	private final DelayQueue<DelayedKey> timeoutQueue = new DelayQueue<>();
	private final Map<String, CacheValue> cacheDatas = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 * @param timeToLiveSeconds Durée de vie des éléments en seconde
	 */
	@Inject
	public MemoryUiSecurityTokenCachePlugin(final @Named("timeToLiveSeconds") int timeToLiveSeconds) {
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String key, final Serializable data) {
		Assertion.checkNotNull(data);
		//---------------------------------------------------------------------
		final CacheValue cacheValue = new CacheValue(data);
		cacheDatas.put(key, cacheValue);
		timeoutQueue.put(new DelayedKey(key, cacheValue.getCreateTime() + timeToLiveSeconds * 1000));
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final String key) {
		final CacheValue cacheValue = cacheDatas.get(key);
		if (cacheValue != null && !isTooOld(cacheValue)) {
			return cacheValue.getValue();
		}
		cacheDatas.remove(key);
		return null; //key expired : return null
	}

	private boolean isTooOld(final CacheValue cacheValue) {
		return System.currentTimeMillis() - cacheValue.getCreateTime() >= timeToLiveSeconds * 1000;
	}

	/** {@inheritDoc} */
	public Serializable getAndRemove(final String key) {
		final Serializable result;
		synchronized (cacheDatas) {
			result = get(key);
			cacheDatas.remove(key);
		}
		return result;
	}

	/**
	 * Purge les elements trop vieux.
	 */
	void removeTooOldElements() {
		final int maxChecked = 500;
		int checked = 0;
		//Les elements sont parcouru dans l'ordre d'insertion (sans lock)		
		while (checked < maxChecked) {
			final DelayedKey delayedKey = timeoutQueue.poll();
			if (delayedKey != null) {
				cacheDatas.remove(delayedKey.getKey());
				checked++;
			} else {
				break;
			}
		}
		logger.info("purge " + checked + " elements");
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

}
