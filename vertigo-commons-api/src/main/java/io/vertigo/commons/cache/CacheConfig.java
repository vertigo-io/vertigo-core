package io.vertigo.commons.cache;

import io.vertigo.lang.Assertion;

/**
 * Config of cache.
 *
 * @author pchretien
 */
public final class CacheConfig {
	private final String cacheType;
	private final int maxElementsInMemory;
	private final long timeToLiveSeconds;
	private final long timeToIdleSeconds;

	public CacheConfig(final String cacheType, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		Assertion.checkArgNotEmpty(cacheType);
		//---------------------------------------------------------------------
		this.cacheType = cacheType;
		this.maxElementsInMemory = maxElementsInMemory;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.timeToIdleSeconds = timeToIdleSeconds;
	}

	public String getCacheType() {
		return cacheType;
	}

	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	public long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	public long getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}
}
