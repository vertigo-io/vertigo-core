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
