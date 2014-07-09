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
package io.vertigo.commons.impl.cache;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * Manager de gestion du cache.
 *
 * @author pchretien
 */
public final class CacheManagerImpl implements CacheManager {
	private final CachePlugin cachePlugin;

	/**
	 * Constructeur.
	 * @param cachePlugin Plugin de gestion du cache
	 */
	@Inject
	public CacheManagerImpl(final CachePlugin cachePlugin) {
		Assertion.checkNotNull(cachePlugin);
		//---------------------------------------------------------------------
		this.cachePlugin = cachePlugin;
	}

	//---------------------------------------------------------------------------
	//------------------Gestion du rendu et des interactions---------------------
	//---------------------------------------------------------------------------

	/** {@inheritDoc} */
	public void addCache(final String cacheType, final String context, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		cachePlugin.addCache(cacheType, context, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds);
	}

	/** {@inheritDoc} */
	public void put(final String context, final Serializable key, final Serializable value) {
		cachePlugin.put(context, key, value);
	}

	/** {@inheritDoc} */
	public Serializable get(final String context, final Serializable key) {
		return cachePlugin.get(context, key);
	}

	/** {@inheritDoc} */
	public boolean remove(final String context, final Serializable key) {
		return cachePlugin.remove(context, key);
	}

	/** {@inheritDoc} */
	public void clear(final String context) {
		cachePlugin.clear(context);
	}

	/** {@inheritDoc} */
	public void clearAll() {
		cachePlugin.clearAll();
	}
}
