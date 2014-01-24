package io.vertigo.plugins.cache.map;

import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation d'un cache memoire.
 * La purge des �l�ments trop vieux se fait lors de la lecture.
 * 
 * @author npiedeloup
 * @version $Id: MapCache.java,v 1.4 2013/10/22 12:38:24 pchretien Exp $
 */
final class MapCache {
	private final String name;
	private final long timeToLiveSeconds;
	private final boolean eternal;
	private long totalHits;
	private long totalCalls;
	//private long totalPuts;
	private final Map<Serializable, CacheValue> cacheDatas = new HashMap<Serializable, CacheValue>();

	/**
	 * Constructeur.
	 * @param name Nom du cache
	 * @param eternal Si eternal
	 * @param timeToLiveSeconds Dur�e de vie en secondes
	 */
	MapCache(final String name, final boolean eternal, final long timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(name);
		//---------------------------------------------------------------------
		this.name = name;
		this.eternal = eternal;
		this.timeToLiveSeconds = timeToLiveSeconds;
	}

	//-------------------------------------------------------------------------
	//-----------------------------Configuration-------------------------------
	//-------------------------------------------------------------------------
	/**
	 * @return Conf : Nom du cache
	 */
	String getName() {
		return name;
	}

	/**
	 * @return Conf : temps de vie en seconde des �l�ments
	 */
	long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return Conf : si cache eternel
	 */
	boolean isEternal() {
		return eternal;
	}

	//-------------------------------------------------------------------------
	//-----------------------------Donn�es-------------------------------------
	//-------------------------------------------------------------------------
	/**
	 * @return Nombre d'�l�ment en cache
	 */
	synchronized int getElementCount() {
		return cacheDatas.size();
	}

	//	/**
	//	 * @return Taille m�moire
	//	 */
	//	long calculateInMemorySize() {
	//		return -1; //TODO il serait bon de pouvoir l'�valuer
	//	}

	/**
	 * @return Nombre de cache hit
	 */
	synchronized long getHits() {
		return totalHits;
	}

	/**
	 * @return Nombre d'appel au cache
	 */
	synchronized long getCalls() {
		return totalCalls;
	}

	/**
	 * @param key Cl� de l'�l�ment
	 * @return �l�ment du cache, null si n'existe pas ou obsolete
	 */
	synchronized Serializable get(final Serializable key) {
		totalCalls++;
		final CacheValue cacheValue = cacheDatas.get(key);
		if (cacheValue != null) {
			if (checkAge(cacheValue)) {
				totalHits++;
				return cacheValue.getValue();
			}
			cacheDatas.remove(key);
		}
		return null;
	}

	private boolean checkAge(final CacheValue cacheValue) {
		return eternal || System.currentTimeMillis() - cacheValue.getCreateTime() < timeToLiveSeconds * 1000;
	}

	/**
	 * @param key Cl� de l'�l�ment
	 * @param value Element
	 */
	synchronized void put(final Serializable key, final Serializable value) {
		//totalPuts++;
		cacheDatas.put(key, new CacheValue(value));
	}

	/**
	 * Vide le cache.
	 */
	synchronized void removeAll() {
		cacheDatas.clear();
	}

	/**
	 * @param key Cl� de l'�l�ment � supprimer
	 * @return si supprim�
	 */
	synchronized boolean remove(final Serializable key) {
		return cacheDatas.remove(key) != null;
	}
}
