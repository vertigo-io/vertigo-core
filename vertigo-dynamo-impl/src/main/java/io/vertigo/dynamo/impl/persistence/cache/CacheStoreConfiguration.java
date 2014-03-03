package io.vertigo.dynamo.impl.persistence.cache;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration des données mises en cache.
 * 
 * @author  pchretien
 * @version $Id: CacheStoreConfiguration.java,v 1.5 2014/01/24 17:59:38 pchretien Exp $
 */
public final class CacheStoreConfiguration {
	/* Liste des DT gérées par le cache, et si le mode de chargement unitaire ou ensembliste. */
	private final Map<DtDefinition, Boolean> cacheableDtDefinitionMap = new HashMap<>();

	/* Délégation de la gestion du cache à un système tiers. */
	private final DataCache dataCache;

	/**
	 * Constructeur.
	 * @param cacheManager Manager du cache
	 */
	public CacheStoreConfiguration(final CacheManager cacheManager) {
		dataCache = new DataCache(cacheManager);
	}

	boolean isCacheable(final DtDefinition dtDefinition) {
		return cacheableDtDefinitionMap.containsKey(dtDefinition);
	}

	DataCache getDataCache() {
		return dataCache;
	}

	/**
	 * Enregistre si un DT peut être mis en cache et la façon de charger les données.
	 * @param dtDefinition Définition de DT
	 * @param timeToLiveInSeconds Durée de vie du cache
	 * @param isReloadedByList Si ce type d'objet doit être chargé de façon ensembliste ou non
	 */
	public void registerCacheable(final DtDefinition dtDefinition, final long timeToLiveInSeconds, final boolean isReloadedByList) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		cacheableDtDefinitionMap.put(dtDefinition, isReloadedByList);
		dataCache.registerContext(dtDefinition, timeToLiveInSeconds);
	}

	/**
	 * @param dtDefinition Définition de DT
	 * @return Si ce type d'objet doit être chargé de façon ensembliste ou non.
	 */
	boolean isReloadedByList(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		return cacheableDtDefinitionMap.get(dtDefinition);
	}
}
