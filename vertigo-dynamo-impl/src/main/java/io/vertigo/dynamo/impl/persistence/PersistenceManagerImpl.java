package io.vertigo.dynamo.impl.persistence;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.impl.persistence.util.BrokerNNImpl;
import io.vertigo.dynamo.persistence.Broker;
import io.vertigo.dynamo.persistence.BrokerConfiguration;
import io.vertigo.dynamo.persistence.BrokerNN;
import io.vertigo.dynamo.persistence.MasterDataConfiguration;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.StorePlugin;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import javax.inject.Inject;

/**
* Implémentation standard du gestionnaire des données et des accès aux données.
*
* @author pchretien
*/
public final class PersistenceManagerImpl implements PersistenceManager, Activeable {
	@Inject
	private Option<FileStorePlugin> fileStorePlugin;

	@Inject
	private StorePlugin defaultStorePlugin;

	private final MasterDataConfiguration masterDataConfiguration;
	private final BrokerConfigurationImpl brokerConfiguration;
	/** Broker des objets métier et des listes. */
	private Broker broker;
	private final BrokerNN brokerNN;

	/**
	 * Constructeur.
	 * @param cacheManager Manager de gestion du cache
	 * @param collectionsManager Manager de gestion des collections
	 */
	@Inject
	public PersistenceManagerImpl(final WorkManager workManager, final CacheManager cacheManager, final CollectionsManager collectionsManager) {
		super();
		Assertion.checkNotNull(collectionsManager);
		//---------------------------------------------------------------------
		masterDataConfiguration = new MasterDataConfigurationImpl(collectionsManager);
		brokerConfiguration = new BrokerConfigurationImpl(cacheManager, this, collectionsManager);
		brokerNN = new BrokerNNImpl(workManager);
	}

	/** {@inheritDoc} */
	public MasterDataConfiguration getMasterDataConfiguration() {
		return masterDataConfiguration;
	}

	/** {@inheritDoc} */
	public void start() {
		//On enregistre le plugin de gestion des fichiers : facultatif
		if (fileStorePlugin.isDefined()) {
			brokerConfiguration.getLogicalFileStoreConfiguration().registerDefaultPhysicalStore(fileStorePlugin.get());
		}
		//On enregistre le plugin principal du broker : DefaultPhysicalStore
		brokerConfiguration.getLogicalStoreConfiguration().registerDefaultPhysicalStore(defaultStorePlugin);
		broker = new BrokerImpl(brokerConfiguration);
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	//	/**
	//	 * @param storePluginId Id du plugin
	//	 * @param dtDefinition DtDefinition
	//	 */
	//	public void definePhysicalStore(final String storePluginId, final DtDefinition dtDefinition) {
	//		final StorePlugin storePlugin = Home.getContainer().getPlugin(storePluginId, StorePlugin.class, PersistenceManager.class);
	//		brokerConfiguration.getLogicalStoreConfiguration().register(dtDefinition, storePlugin);
	//	}

	/**
	 * @return Configuration du PersistenceManager
	 */
	public BrokerConfiguration getBrokerConfiguration() {
		return brokerConfiguration;
	}

	//-------------------------------------------------------------------------

	/** {@inheritDoc} */
	public Broker getBroker() {
		return broker;
	}

	public BrokerNN getBrokerNN() {
		return brokerNN;
	}
}
