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

import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation d'un cache memoire.
 * La purge des éléments trop vieux se fait lors de la lecture.
 *
 * @author npiedeloup
 */
final class MapCache {
	private final String name;
	private final long timeToLiveSeconds;
	private final boolean eternal;
	private long totalHits;
	private long totalCalls;
	//private long totalPuts;
	private final Map<Serializable, CacheValue> cacheDatas = new HashMap<>();

	/**
	 * Constructeur.
	 * @param name Nom du cache
	 * @param eternal Si eternal
	 * @param timeToLiveSeconds Durée de vie en secondes
	 */
	MapCache(final String name, final boolean eternal, final long timeToLiveSeconds) {
		Assertion.checkArgNotEmpty(name);
		//-----
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
	 * @return Conf : temps de vie en seconde des éléments
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

	//=========================================================================
	//=============================Données=====================================
	//=========================================================================
	/**
	 * @return Nombre d'élément en cache
	 */
	synchronized int getElementCount() {
		return cacheDatas.size();
	}

	//	/**
	//	 * @return Taille mémoire
	//	 */
	//	long calculateInMemorySize() {
	//		return -1; //TODO il serait bon de pouvoir l'évaluer
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
	 * @param key Clé de l'élément
	 * @return élément du cache, null si n'existe pas ou obsolete
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
	 * @param key Clé de l'élément
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
	 * @param key Clé de l'élément à supprimer
	 * @return si supprimé
	 */
	synchronized boolean remove(final Serializable key) {
		return cacheDatas.remove(key) != null;
	}
}
