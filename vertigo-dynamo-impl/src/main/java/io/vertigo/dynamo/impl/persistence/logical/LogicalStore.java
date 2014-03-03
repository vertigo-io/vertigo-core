package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIAll;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.Broker;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.Store;
import io.vertigo.kernel.lang.Assertion;

/**
 * Permet de gérer les listes de référence.
 * Transpose en store physique les appels logiques.
 *
 * @author  pchretien
 * @version $Id: LogicalStore.java,v 1.5 2014/01/20 17:49:32 pchretien Exp $
 */
public final class LogicalStore implements Store {
	private final LogicalStoreConfiguration logicalStoreConfiguration;
	private final Broker broker;

	/**
	 * Constructeur.
	 * @param logicalStoreConfiguration Configuration logique des stores physiques.
	 * @param broker Broker pour réentrance
	 */
	public LogicalStore(final LogicalStoreConfiguration logicalStoreConfiguration, final Broker broker) {
		Assertion.checkNotNull(logicalStoreConfiguration);
		Assertion.checkNotNull(broker);
		//---------------------------------------------------------------------
		this.logicalStoreConfiguration = logicalStoreConfiguration;
		this.broker = broker;
	}

	private static DtDefinition getDtDefinition(final URI<? extends DtObject> uri) {
		return uri.getDefinition();
	}

	private Store getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfiguration.getPhysicalStore(dtDefinition);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		if (uri instanceof DtListURIForMasterData) {
			return loadMDList((DtListURIForMasterData) uri);
		}
		return getPhysicalStore(uri.getDtDefinition()).<D> loadList(uri);
	}

	private <D extends DtObject> DtList<D> loadMDList(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		//On cherche la liste complete (URIAll n'est pas une DtListURIForMasterData pour ne pas boucler)
		final DtList<D> unFilteredDtc = broker.<D> getList(new DtListURIAll(uri.getDtDefinition()));

		//Composition.
		final Function<DtList<D>, DtList<D>> filterFunction = logicalStoreConfiguration.getPersistenceManager().getMasterDataConfiguration().<D> getFilter(uri);
		final Function<DtList<D>, DtList<D>> sortFunction = logicalStoreConfiguration.getCollectionsManager().createSort(uri.getDtDefinition().getSortField().get().getName(), false, true, true);
		//On compose les fonctions 
		//1.on filtre 
		//2.on trie
		final DtList<D> sortedDtc = sortFunction.apply(filterFunction.apply(unFilteredDtc));
		sortedDtc.setURI(uri);
		return sortedDtc;
	}

	/** {@inheritDoc} */
	public <D extends DtObject> D load(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = getDtDefinition(uri);
		return getPhysicalStore(dtDefinition).<D> load(uri);
	}

	/** {@inheritDoc} */
	public void merge(final DtObject dto) {
		getPhysicalStore(DtObjectUtil.findDtDefinition(dto)).merge(dto);
	}

	/** {@inheritDoc} */
	public void put(final DtObject dto) {
		getPhysicalStore(DtObjectUtil.findDtDefinition(dto)).put(dto);
	}

	/** {@inheritDoc} */
	public void remove(final URI<? extends DtObject> uri) {
		final DtDefinition dtDefinition = getDtDefinition(uri);
		getPhysicalStore(dtDefinition).remove(uri);
	}

	/** {@inheritDoc} */
	@Deprecated
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		return getPhysicalStore(dtDefinition).loadList(dtDefinition, criteria, maxRows);
	}

	/** {@inheritDoc} */
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}
}
