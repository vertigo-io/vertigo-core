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

import io.vertigo.core.spaces.component.ComponentInitializer;

/**
 * Initialisation du manager des caches.
 * @author dchallas
 */
public final class CacheManagerInitializer implements ComponentInitializer<CacheManager> {
	public static final String CONTEXT = "testCacheManager";

	/** {@inheritDoc} */
	public void init(final CacheManager manager) {
		//Paramétrage d'un cache spécifique au test
		/** Parametre du cache, pour une config ou il est multi-session*/
		final int maxElementsInMemory = 5000;
		final long timeToLiveSeconds = 10; //longévité d'un élément
		final long timeToIdleSeconds = 10; //longévité d'un élément non utilisé

		manager.addCache(CONTEXT, new CacheConfig("test", maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds));
	}
}
