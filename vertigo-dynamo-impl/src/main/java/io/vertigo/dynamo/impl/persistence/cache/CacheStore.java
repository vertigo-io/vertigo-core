package io.vertigo.dynamo.impl.persistence.cache;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIAll;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.Store;
import io.vertigo.kernel.lang.Assertion;

/**
 * Gestion des données mises en cache.
 * 
 * @author  pchretien
 */
public final class CacheStore implements Store {
	private final Store logicalStore;
	private final CacheStoreConfiguration cacheStoreConfiguration;

	/**
	 * Constructeur.
	 * @param logicalStore Store logique
	 * @param cacheStoreConfiguration Configuration du cache
	 */
	public CacheStore(final Store logicalStore, final CacheStoreConfiguration cacheStoreConfiguration) {
		Assertion.checkNotNull(cacheStoreConfiguration);
		Assertion.checkNotNull(logicalStore);
		//---------------------------------------------------------------------
		this.logicalStore = logicalStore;
		this.cacheStoreConfiguration = cacheStoreConfiguration;
	}

	/** {@inheritDoc} */
	public <D extends DtObject> D load(final URI<D> uri) {
		// - Prise en compte du cache
		if (cacheStoreConfiguration.isCacheable(uri.<DtDefinition> getDefinition())) {
			D dto = cacheStoreConfiguration.getDataCache().<D> getDtObject(uri);
			if (dto == null) {
				//Cas ou le dto représente un objet non mis en cache
				dto = this.<D> reload(uri);
			}
			return dto;
		}
		//Si on ne récupère rien dans le cache on charge depuis le store.
		return logicalStore.<D> load(uri);
	}

	private synchronized <D extends DtObject> D reload(final URI<D> uri) {
		final D dto;
		if (cacheStoreConfiguration.isReloadedByList(uri.<DtDefinition> getDefinition())) {
			//On ne charge pas les cache de façon atomique.
			final DtListURI dtcURIAll = new DtListURIAll(uri.<DtDefinition> getDefinition());
			reloadList(dtcURIAll); //on charge la liste complete (et on remplit les caches)
			dto = cacheStoreConfiguration.getDataCache().<D> getDtObject(uri);
		} else {
			//On charge le cache de façon atomique.
			dto = logicalStore.<D> load(uri);
			cacheStoreConfiguration.getDataCache().putDtObject(dto);
		}
		return dto;
	}

	/** {@inheritDoc}  */
	public <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		// - Prise en compte du cache
		//On ne met pas en cache les URI d'une association NN
		if (cacheStoreConfiguration.isCacheable(uri.getDtDefinition()) && !isMultipleAssociation(uri)) {
			DtList<D> dtc = cacheStoreConfiguration.getDataCache().getDtList(uri);
			if (dtc == null) {
				dtc = this.<D> reloadList(uri);
			}
			return dtc;
		}
		//Si la liste n'est pas dans le cache alors on lit depuis le store.
		return logicalStore.<D> loadList(uri);
	}

	private static boolean isMultipleAssociation(final DtListURI uri) {
		if (uri instanceof DtListURIForAssociation) {
			final DtListURIForAssociation uriForAssociation = (DtListURIForAssociation) uri;
			return !uriForAssociation.getAssociationDefinition().isAssociationSimpleDefinition();
		}
		return false;
	}

	private synchronized <D extends DtObject> DtList<D> reloadList(final DtListURI uri) {
		// On charge la liste initiale avec les critéres définis en amont
		final DtList<D> dtc = logicalStore.loadList(uri);
		// Mise en cache de la liste et des éléments.
		cacheStoreConfiguration.getDataCache().putDtList(dtc);
		return dtc;
	}

	@Deprecated
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		//loadCache(dtDefinition);
		return logicalStore.loadList(dtDefinition, criteria, maxRows);
	}

	//==============================Fonctions d'écriture=======================
	/** {@inheritDoc} */
	public void merge(final DtObject dto) {
		logicalStore.merge(dto);
		clearCache(DtObjectUtil.findDtDefinition(dto));
	}

	/** {@inheritDoc} */
	public void put(final DtObject dto) {
		logicalStore.put(dto);
		//La mise à jour d'un seul élément suffit à rendre le cache obsolète
		clearCache(DtObjectUtil.findDtDefinition(dto));
	}

	/** {@inheritDoc} */
	public void remove(final URI<? extends DtObject> uri) {
		final DtDefinition dtDefinition = uri.getDefinition();
		logicalStore.remove(uri);
		clearCache(dtDefinition);
	}

	/* On notifie la mise à jour du cache, celui-ci est donc vidé. */
	private void clearCache(final DtDefinition dtDefinition) {
		if (cacheStoreConfiguration.isCacheable(dtDefinition)) {
			cacheStoreConfiguration.getDataCache().clear(dtDefinition);
		}
	}

	/** {@inheritDoc} */
	public int count(final DtDefinition dtDefinition) {
		return logicalStore.count(dtDefinition);
	}
}
