package io.vertigo.plugins.commons.cache.map;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commonsimpl.cache.CachePlugin;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Modifiable;
import io.vertigo.kernel.lang.Option;

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
 * Impl�mentation MapCache du plugins.
 *
 * @author npiedeloup
 * @version $Id: MapCachePlugin.java,v 1.11 2014/01/23 11:50:36 pchretien Exp $
 */
public final class MapCachePlugin implements CachePlugin, Describable {
	private final CodecManager codecManager;
	private final Map<String, List<String>> cacheTypeMap = new LinkedHashMap<String, List<String>>();
	private final Map<String, MapCache> cachesPerContext = new HashMap<String, MapCache>();
	private final Set<String> noSerializationContext;

	/**
	 * Constructeur.
	 * @param codecManager Manager des m�canismes de codage/d�codage. 
	 * @param noSerializationOption Liste optionnelles des noms de context � ne jamais s�rializer
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
		//Si l'objet est bien marqu� non modifiable (ie : interface Modifiable ET !isModifiable)
		//on peut le garder tel quel, sinon on le clone
		//TODO � revoir : les DtObject et DtList ne peuvent plus etre non Modifiable, on ajoute un param�trage sp�cifique 
		if (isUnmodifiable(value) || noSerializationContext.contains(context)) {
			putElement(context, key, value);
		} else {
			// S�rialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode(value);
			//La s�rialisation est �quivalemnte � un deep Clone.
			putElement(context, key, serializedObject);
		}
	}

	private boolean isUnmodifiable(final Serializable value) {
		//s'il n'implemente pas Modifiable, il doit �tre clon�
		//s'il implemente Modifiable et que isModifiable == true, il doit �tre clon� 
		return value instanceof Modifiable && !((Modifiable) value).isModifiable();
	}

	/** {@inheritDoc} */
	public Serializable get(final String context, final Serializable key) {
		final Serializable cachedObject = getElement(context, key);
		//on ne connait pas l'�tat Modifiable ou non de l'objet, on se base sur son type.
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
		Assertion.checkNotNull(mapCache, "Le cache {0} n''a pas �t� cr�e.", context);
		return mapCache;
	}

	/**
	 * Conserve la liste des caches par type.
	 * D�j� synchroniz� depuis le addCache.
	 * @param cacheName Nom du cache
	 * @param cacheType Type du cache
	 */
	private void registerCacheType(final String cacheName, final String cacheType) {
		List<String> cacheNameList = cacheTypeMap.get(cacheType);
		if (cacheNameList == null) {
			cacheNameList = new ArrayList<String>();
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
