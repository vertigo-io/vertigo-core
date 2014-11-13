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
package io.vertigo.dynamo.impl.persistence.logical;

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
import io.vertigo.dynamo.persistence.DataStore;
import io.vertigo.lang.Assertion;

/**
 * Permet de gérer les listes de référence.
 * Transpose en store physique les appels logiques.
 *
 * @author  pchretien
 */
public final class LogicalDataStore implements DataStore {
	private final LogicalStoreConfiguration logicalStoreConfiguration;
	private final Broker broker;

	/**
	 * Constructeur.
	 * @param logicalStoreConfiguration Configuration logique des stores physiques.
	 * @param broker Broker pour réentrance
	 */
	public LogicalDataStore(final LogicalStoreConfiguration logicalStoreConfiguration, final Broker broker) {
		Assertion.checkNotNull(logicalStoreConfiguration);
		Assertion.checkNotNull(broker);
		//---------------------------------------------------------------------
		this.logicalStoreConfiguration = logicalStoreConfiguration;
		this.broker = broker;
	}

	private static DtDefinition getDtDefinition(final URI<? extends DtObject> uri) {
		return uri.getDefinition();
	}

	private DataStore getPhysicalStore(final DtDefinition dtDefinition) {
		return logicalStoreConfiguration.getPhysicalStore(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
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
		Assertion.checkArgument(uri.getDtDefinition().getSortField().isDefined(), "Sortfield on definition {0} wasn't set. It's mandatory for MasterDataList.", uri.getDtDefinition().getName());
		//---------------------------------------------------------------------
		//On cherche la liste complete (URIAll n'est pas une DtListURIForMasterData pour ne pas boucler)
		final DtList<D> unFilteredDtc = broker.<D> getList(new DtListURIAll(uri.getDtDefinition()));

		//Composition.
		//On compose les fonctions
		//1.on filtre
		//2.on trie
		final DtList<D> sortedDtc = logicalStoreConfiguration.getPersistenceManager().getMasterDataConfiguration().getFilter(uri)//
				.sort(uri.getDtDefinition().getSortField().get().getName(), false, true, true)//
				.apply(unFilteredDtc);
		sortedDtc.setURI(uri);
		return sortedDtc;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D load(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = getDtDefinition(uri);
		return getPhysicalStore(dtDefinition).<D> load(uri);
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		getPhysicalStore(DtObjectUtil.findDtDefinition(dto)).merge(dto);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final DtObject dto) {
		getPhysicalStore(DtObjectUtil.findDtDefinition(dto)).put(dto);
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final URI<? extends DtObject> uri) {
		final DtDefinition dtDefinition = getDtDefinition(uri);
		getPhysicalStore(dtDefinition).remove(uri);
	}

	/** {@inheritDoc} */
	@Deprecated
	@Override
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		return getPhysicalStore(dtDefinition).loadList(dtDefinition, criteria, maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return getPhysicalStore(dtDefinition).count(dtDefinition);
	}
}
