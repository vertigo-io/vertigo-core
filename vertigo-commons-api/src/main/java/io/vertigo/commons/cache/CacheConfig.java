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
package io.vertigo.commons.cache;

import io.vertigo.lang.Assertion;

/**
 * The cacheConfig class defines the configuration of cache.
 *
 * These 3 params define the strategy of caching
 *  - max elements in memory
 *  - time to live
 *  - time to idle
 *
 * @author pchretien
 */
public final class CacheConfig {
	private final String cacheType;
	private final boolean serializeElements;
	private final int maxElementsInMemory;
	private final long timeToLiveSeconds;
	private final long timeToIdleSeconds;

	/**
	 * Constructor.
	 * @param cacheType Type of cache.
	 * @param serializeElements If elements are serialized
	 * @param maxElementsInMemory Max elements stored in memory
	 * @param timeToLiveSeconds Time to live (in seconds)
	 * @param timeToIdleSeconds Time to live when idle (in seconds)
	 */
	public CacheConfig(final String cacheType, final boolean serializeElements, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		Assertion.checkArgNotEmpty(cacheType);
		//-----
		this.cacheType = cacheType;
		this.serializeElements = serializeElements;
		this.maxElementsInMemory = maxElementsInMemory;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.timeToIdleSeconds = timeToIdleSeconds;
	}

	/**
	 * @return Type of cache
	 */
	public String getCacheType() {
		return cacheType;
	}

	/**
	 * @return elements should be serialized
	 */
	public boolean shouldSerializeElements() {
		return serializeElements;
	}

	/**
	 * @return Max elements stored in memory
	 */
	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}

	/**
	 * @return Time to live (in seconds)
	 */
	public long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return Time tio idle (in seconds)
	 */
	public long getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}
}
