package io.vertigo.struts2.plugins.cache.ehcache;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.lang.Activeable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Modifiable;
import io.vertigo.core.lang.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Implémentation EHCache du CacheManager.
 *
 * @author pchretien, npiedeloup
 */
public final class EhCachePlugin implements Activeable, CachePlugin {

	private net.sf.ehcache.CacheManager manager;
	private final CodecManager codecManager;
	private final Map<String, List<String>> cacheTypeMap = new LinkedHashMap<>();
	private final Set<String> noSerializationContext;

	/**
	 * Constructeur.
	 * @param codecManager CodecManager
	 * @param noSerializationOption Liste optionnelles des noms de classes à ne jamais sérialiser
	 * @throws ClassNotFoundException Si une des classes à ne pas sérialiser est inconnue
	 */
	@Inject
	public EhCachePlugin(final CodecManager codecManager, @Named("noSerialization") final Option<String> noSerializationOption) throws ClassNotFoundException {
		Assertion.checkNotNull(codecManager);
		Assertion.checkNotNull(noSerializationOption);
		//---------------------------------------------------------------------
		this.codecManager = codecManager;
		if (noSerializationOption.isDefined()) {
			noSerializationContext = new HashSet<>(Arrays.asList(noSerializationOption.get().split(";")));
		} else {
			noSerializationContext = Collections.emptySet();
		}

	}

	/** {@inheritDoc} */
	@Override
	public void addCache(final String cacheType, final String context, final int maxElementsInMemory, final long timeToLiveSeconds, final long timeToIdleSeconds) {
		if (!manager.cacheExists(context)) {
			final boolean overflowToDisk = true;
			final boolean eternal = false;
			final net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(context, maxElementsInMemory, overflowToDisk, eternal, timeToLiveSeconds, timeToIdleSeconds);
			manager.addCache(cache);
		}
		registerCacheType(context, cacheType);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Serializable value) {
		Assertion.checkState(!(value instanceof byte[]), "Ce CachePlugin ne permet pas de mettre en cache des byte[].");
		//---------------------------------------------------------------------
		//Si l'objet est bien marqué non modifiable (ie : interface Modifiable ET !isModifiable)
		//on peut le garder tel quel, sinon on le clone
		if (isUnmodifiable(value) || noSerializationContext.contains(context)) {
			putEH(context, key, value);
		} else {
			// Sérialisation avec compression
			final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode(value);
			//La sérialisation est équivalente à un deep Clone.
			putEH(context, key, serializedObject);
		}
	}

	private static boolean isUnmodifiable(final Serializable value) {
		//s'il n'implemente pas Modifiable, il doit être cloné
		//s'il implemente Modifiable et que isModifiable == true, il doit être cloné 
		return value instanceof Modifiable && !((Modifiable) value).isModifiable();
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final String context, final Serializable key) {
		final Serializable cachedObject = getEH(context, key);
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
		getEHCache(context).removeAll();
	}

	private void putEH(final String context, final Object key, final Serializable serialized) {
		final Element element = new Element(key, serialized);
		getEHCache(context).put(element);
	}

	private Serializable getEH(final String context, final Object key) {
		final Element element = getEHCache(context).get(key);
		return element == null ? null : element.getValue();
	}

	private Ehcache getEHCache(final String context) {
		final Ehcache ehCache = manager.getCache(context);
		Assertion.checkNotNull(ehCache, "Le cache {0} n''a pas été déclaré. Ajouter le dans un fichier ehcache.xml dans le répertoire WEB-INF de votre webbapp.", context);
		return ehCache;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		manager = net.sf.ehcache.CacheManager.create();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		manager.shutdown();
	}

	private void registerCacheType(final String context, final String cacheType) {
		List<String> cacheNameList = cacheTypeMap.get(cacheType);
		if (cacheNameList == null) {
			cacheNameList = new ArrayList<>();
			cacheTypeMap.put(cacheType, cacheNameList);
		}
		cacheNameList.add(context);
	}

}
