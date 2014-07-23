package io.vertigo.rest.plugins.rest.security.memory;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.impl.security.UiSecurityTokenCachePlugin;

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
 * @version $Id: BerkeleyContextCachePlugin.java,v 1.6 2014/03/05 11:23:17 npiedeloup Exp $
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
