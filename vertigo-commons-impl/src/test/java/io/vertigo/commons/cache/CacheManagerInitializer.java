package io.vertigo.commons.cache;

import io.vertigo.kernel.component.ComponentInitializer;

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

		manager.addCache("test", CONTEXT, maxElementsInMemory, timeToLiveSeconds, timeToIdleSeconds);
	}
}
