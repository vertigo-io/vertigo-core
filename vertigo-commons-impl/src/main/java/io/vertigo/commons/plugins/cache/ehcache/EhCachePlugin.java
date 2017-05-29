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
package io.vertigo.commons.plugins.cache.ehcache;

import java.io.Serializable;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.cache.CacheDefinition;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.component.Activeable;
import io.vertigo.lang.Assertion;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Implémentation EHCache du CacheManager.
 *
 * @author pchretien, npiedeloup
 */
public final class EhCachePlugin implements Activeable, CachePlugin {
	private final net.sf.ehcache.CacheManager manager;
	private final CodecManager codecManager;

	/**
	 * Constructor.
	 * @param codecManager CodecManager
	 */
	@Inject
	public EhCachePlugin(final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		manager = net.sf.ehcache.CacheManager.create();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		registerCaches();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		manager.shutdown();
	}

	private void registerCaches() {
		Home.getApp().getDefinitionSpace()
				.getAll(CacheDefinition.class).stream()
				.forEach(this::registerCache);
	}

	private void registerCache(final CacheDefinition cacheDefinition) {
		if (!manager.cacheExists(cacheDefinition.getName())) {
			final boolean overflowToDisk = cacheDefinition.shouldSerializeElements(); //don't overflow
			final net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheDefinition.getName(),
					cacheDefinition.getMaxElementsInMemory(),
					overflowToDisk,
					false, //not eternal
					cacheDefinition.getTimeToLiveSeconds(),
					cacheDefinition.getTimeToIdleSeconds());
			manager.addCache(cache);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String cacheName, final Serializable key, final Object value) {
		Assertion.checkNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", cacheName, key);
		Assertion.checkState(!(value instanceof byte[]), "CachePlugin can't cache byte[] values");
		//-----
		//On regarde la conf du cache pour vérifier s'il on serialize/clone les éléments ou non.
		if (getCacheDefinition(cacheName).shouldSerializeElements()) {
			Assertion.checkArgument(value instanceof Serializable,
					"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
					cacheName, key, value.getClass().getSimpleName());
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
			//La sérialisation est équivalente à un deep Clone.
			putEH(cacheName, key, serializedObject);
		} else {
			putEH(cacheName, key, value);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final Object cachedObject = getEH(context, key);
		//on ne connait pas l'état Modifiable ou non de l'objet, on se base sur son type.
		if (cachedObject instanceof byte[]) {
			final byte[] serializedObject = (byte[]) cachedObject;
			return codecManager.getCompressedSerializationCodec().decode(serializedObject);
		}
		return cachedObject;
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		return getEHCache(context).remove(key);
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		manager.clearAll();
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		final Ehcache ehCache = manager.getCache(context);
		if (ehCache != null) {
			ehCache.removeAll();
		}
	}

	private void putEH(final String context, final Object key, final Object value) {
		final Element element = new Element(key, value);
		getEHCache(context).put(element);
	}

	private Object getEH(final String context, final Object key) {
		final Element element = getEHCache(context).get(key);
		return element == null ? null : element.getObjectValue();
	}

	private Ehcache getEHCache(final String context) {
		final Ehcache ehCache = manager.getCache(context);
		Assertion.checkNotNull(ehCache, "Cache {0} are not yet registered. Add it into a file ehcache.xml and put it into the WEB-INF directory of your webapp.", context);
		return ehCache;
	}

	private CacheDefinition getCacheDefinition(final String cacheName) {
		return Home.getApp().getDefinitionSpace().resolve(cacheName, CacheDefinition.class);
	}
}
