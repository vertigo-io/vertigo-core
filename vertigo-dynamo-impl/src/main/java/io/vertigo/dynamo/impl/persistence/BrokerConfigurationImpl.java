package io.vertigo.dynamo.impl.persistence;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.impl.persistence.cache.CacheStoreConfiguration;
import io.vertigo.dynamo.impl.persistence.logical.LogicalFileStoreConfiguration;
import io.vertigo.dynamo.impl.persistence.logical.LogicalStoreConfiguration;
import io.vertigo.dynamo.persistence.BrokerConfiguration;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.StorePlugin;
import io.vertigo.kernel.lang.Assertion;

/**
 * Implémentation Standard du StoreProvider.
 * 
 * @author pchretien
 * @version $Id: BrokerConfigurationImpl.java,v 1.5 2014/01/24 17:59:38 pchretien Exp $
 */
final class BrokerConfigurationImpl implements BrokerConfiguration {
	private final CacheStoreConfiguration cacheStoreConfiguration;
	private final LogicalStoreConfiguration logicalStoreConfiguration;
	private final LogicalFileStoreConfiguration logicalFileStoreConfiguration;

	/**
	 * Constructeur.
	 * 
	 * @param cacheManager Manager de gestion du cache
	 */
	BrokerConfigurationImpl(final CacheManager cacheManager, final PersistenceManager persistenceManager, final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		cacheStoreConfiguration = new CacheStoreConfiguration(cacheManager);
		logicalStoreConfiguration = new LogicalStoreConfiguration(persistenceManager, collectionsManager);
		logicalFileStoreConfiguration = new LogicalFileStoreConfiguration();
	}

	/**
	 * @param fileInfoDefinition Definition de fichier
	 * @param newFileStore Store de fichier
	 */
	void registerFileStorePlugin(final FileInfoDefinition fileInfoDefinition, final FileStore newFileStore) {
		getLogicalFileStoreConfiguration().register(fileInfoDefinition, newFileStore);
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
		cacheStoreConfiguration.registerCacheable(dtDefinition, timeToLiveInSeconds, isReloadedByList);
	}

	CacheStoreConfiguration getCacheStoreConfiguration() {
		return cacheStoreConfiguration;
	}

	LogicalStoreConfiguration getLogicalStoreConfiguration() {
		return logicalStoreConfiguration;
	}

	LogicalFileStoreConfiguration getLogicalFileStoreConfiguration() {
		return logicalFileStoreConfiguration;
	}

	public void register(final DtDefinition dtDefinition, final StorePlugin specificStore) {
		getLogicalStoreConfiguration().register(dtDefinition, specificStore);
	}
}
