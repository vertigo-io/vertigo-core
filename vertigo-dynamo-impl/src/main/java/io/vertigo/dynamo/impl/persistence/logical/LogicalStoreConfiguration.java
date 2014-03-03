package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.persistence.StorePlugin;
import io.vertigo.kernel.lang.Assertion;

/**
 * Configuration logique des stores physiques.
 * @author pchretien
 * @version $Id: LogicalStoreConfiguration.java,v 1.4 2014/01/20 17:49:32 pchretien Exp $
 */
public final class LogicalStoreConfiguration extends AbstractLogicalStoreConfiguration<DtDefinition, StorePlugin> {
	private final PersistenceManager persistenceManager;
	private final CollectionsManager collectionsManager;

	/**
	 * Constructeur.
	 * @param collectionsManager Manager des manipulations de liste
	 */
	public LogicalStoreConfiguration(final PersistenceManager persistenceManager, final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(collectionsManager);
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
		this.collectionsManager = collectionsManager;
	}

	PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	CollectionsManager getCollectionsManager() {
		return collectionsManager;
	}
}
