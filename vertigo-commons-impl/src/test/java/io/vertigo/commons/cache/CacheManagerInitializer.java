/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Inject;

import io.vertigo.core.component.ComponentInitializer;

/**
 * Initialisation du manager des caches.
 * @author dchallas
 */
public final class CacheManagerInitializer implements ComponentInitializer {

	/** Cache context */
	public static final String CONTEXT_EDITABLE = "testCacheEditableElements";
	public static final String CONTEXT_READONLY = "testCacheReadOnlyElements";

	@Inject
	private CacheManager manager;

	/** {@inheritDoc} */
	@Override
	public void init() {
		//Paramétrage d'un cache spécifique au test
		/** Parametre du cache, pour une config ou il est multi-session*/
		final int maxElementsInMemory = 50000;
		final int timeToLiveSeconds = 1000; //longévité d'un élément
		final int timeToIdleSeconds = 10; //longévité d'un élément non utilisé

		manager.addCache(CONTEXT_EDITABLE, new CacheConfig("test", true, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds));
		manager.addCache(CONTEXT_READONLY, new CacheConfig("test", false, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds));
	}
}
