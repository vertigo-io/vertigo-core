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
package io.vertigo.commons.plugins.cache.map;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.component.ComponentInfo;
import io.vertigo.core.component.Describable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Modifiable;
import io.vertigo.core.lang.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation MapCache du plugins.
 *
 * @author npiedeloup
 */
public final class MapCachePlugin implements CachePlugin, Describable {
	private final CodecManager codecManager;
	private final Map<String, List<String>> cacheTypeMap = new LinkedHashMap<>();
	private final Map<String, MapCache> cachesPerContext = new HashMap<>();
	private final Set<String> noSerializationContext;

	/**
	 * Constructeur.
	 * @param codecManager Manager des mécanismes de codage/décodage. 
	 * @param noSerializationOption Liste optionnelles des noms de context à ne jamais sérialiser
	 */
	@Inject
	public MapCachePlugin(final CodecManager codecManager, @Named("noSerialization") final Option<String> noSerializationOption) {
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		this.codecManager = codecManager;
		if (noSerializationOption.isDefined()) {
			noSerializationContext = new HashSet<>(Arrays.asList(noSerializationOption.get().split(";")));
		} else {
			noSerializationContext = Collections.emptySet();
		}
	}

	/** {@inheritDoc} */
	public void addCache(final String cacheType, final String context, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		if (!cachesPerContext.containsKey(context)) {
			final boolean eternal = false;
			final MapCache cache = new MapCache(context, eternal, timeToLiveSeconds);
			cachesPerContext.put(context, cache);
		}
		registerCacheType(context, cacheType);
	}

	/** {@inheritDoc} */
	public void put(final String context, final Serializable key, final Serializable value) {
		Assertion.checkArgument(!(value instanceof byte[]), "Ce CachePlugin ne permet pas de mettre en cache des byte[].");
		//---------------------------------------------------------------------
		//Si l'objet est bien marqué non modifiable (ie : interface Modifiable ET !isModifiable)
		//on peut le garder tel quel, sinon on le clone
		//TODO à revoir : les DtObject et DtList ne peuvent plus etre non Modifiable, on ajoute un paramétrage spécifique 
		if (isUnmodifiable(value) || noSerializationContext.contains(context)) {
			putElement(context, key, value);
		} else {
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode(value);
			//La sérialisation est équivalente à un deep Clone.
			putElement(context, key, serializedObject);
		}
	}

	private boolean isUnmodifiable(final Serializable value) {
		//s'il n'implemente pas Modifiable, il doit être cloné
		//s'il implemente Modifiable et que isModifiable == true, il doit être cloné 
		return value instanceof Modifiable && !((Modifiable) value).isModifiable();
	}

	/** {@inheritDoc} */
	public Serializable get(final String context, final Serializable key) {
		final Serializable cachedObject = getElement(context, key);
		//on ne connait pas l'état Modifiable ou non de l'objet, on se base sur son type.
		if (cachedObject instanceof byte[]) {
			final byte[] serializedObject = (byte[]) cachedObject;
			return codecManager.getCompressedSerializationCodec().decode(serializedObject);
		}
		return cachedObject;
	}

	/** {@inheritDoc} */
	public boolean remove(final String context, final Serializable key) {
		return getMapCache(context).remove(key);
	}

	/** {@inheritDoc} */
	public synchronized void clearAll() {
		for (final MapCache mapCache : cachesPerContext.values()) {
			mapCache.removeAll();
		}
	}

	/** {@inheritDoc} */
	public void clear(final String context) {
		getMapCache(context).removeAll();
	}

	private void putElement(final String context, final Serializable key, final Serializable value) {
		getMapCache(context).put(key, value);
	}

	private Serializable getElement(final String context, final Serializable key) {
		return getMapCache(context).get(key);
	}

	private synchronized MapCache getMapCache(final String context) {
		final MapCache mapCache = cachesPerContext.get(context);
		Assertion.checkNotNull(mapCache, "Le cache {0} n''a pas été crée.", context);
		return mapCache;
	}

	/**
	 * Conserve la liste des caches par type.
	 * Déjà synchronisé depuis le addCache.
	 * @param cacheName Nom du cache
	 * @param cacheType Type du cache
	 */
	private void registerCacheType(final String cacheName, final String cacheType) {
		List<String> cacheNameList = cacheTypeMap.get(cacheType);
		if (cacheNameList == null) {
			cacheNameList = new ArrayList<>();
			cacheTypeMap.put(cacheType, cacheNameList);
		}
		cacheNameList.add(cacheName);
	}

	/** {@inheritDoc} */
	public List<ComponentInfo> getInfos() {
		long hits = 0L;
		long calls = 0L;
		//---
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		for (final String cacheName : cachesPerContext.keySet()) {
			final MapCache mapCache = getMapCache(cacheName);
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".elements", mapCache.getElementCount()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".hits", mapCache.getHits()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".calls", mapCache.getCalls()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".ttl", mapCache.getTimeToLiveSeconds()));
			componentInfos.add(new ComponentInfo("cache." + cacheName + ".eternal", mapCache.isEternal()));
			hits += mapCache.getHits();
			calls += mapCache.getCalls();
		}
		final double ratio = 100d * (calls > 0 ? hits / calls : 1);//Par convention 100% 
		componentInfos.add(new ComponentInfo("cache.hits", hits));
		componentInfos.add(new ComponentInfo("cache.calls", calls));
		componentInfos.add(new ComponentInfo("cache.ratio", ratio));

		return componentInfos;
	}
}
