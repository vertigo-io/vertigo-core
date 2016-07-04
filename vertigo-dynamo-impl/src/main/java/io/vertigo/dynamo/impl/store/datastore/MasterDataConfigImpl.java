/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.store.datastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.lang.Assertion;

/**
 * Configuration des listes de référence.
 * @author pchretien
 */
public final class MasterDataConfigImpl implements MasterDataConfig {

	/**
	 * Fonction de DtList acceptant tout (pour rester not null).
	 */
	private final DtListProcessor identityFunction;

	/** CollectionsManager.*/
	private final CollectionsManager collectionsManager;

	private final Map<DtListURIForMasterData, DtListProcessor> mdlUriFilterMap = new HashMap<>();
	private final Map<DtDefinition, DtListURIForMasterData> defaultMdlMap2 = new HashMap<>();

	/**
	 * Constructeur.
	 * @param collectionsManager Manager des collections
	 */
	public MasterDataConfigImpl(final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(collectionsManager);
		//-----
		this.collectionsManager = collectionsManager;
		//identity function do nothing
		identityFunction = collectionsManager.createDtListProcessor();
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri, final String fieldName, final Serializable value) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(fieldName);
		//-----
		final DtListProcessor dtListFilter = collectionsManager.createDtListProcessor()
				.filterByValue(fieldName, value);
		register(uri, dtListFilter);
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri, final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(fieldName1);
		Assertion.checkNotNull(fieldName2);
		//-----
		final DtListProcessor dtListFilter = collectionsManager.createDtListProcessor()
				.filterByValue(fieldName1, value1)
				.filterByValue(fieldName2, value2);
		register(uri, dtListFilter);
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		//-----
		register(uri, identityFunction);
	}

	private void register(final DtListURIForMasterData uri, final DtListProcessor dtListFilter) {
		Assertion.checkNotNull(uri);
		Assertion.checkArgument(!mdlUriFilterMap.containsKey(uri), "Il existe deja une liste de référence enregistrée {0}.", uri);
		//Criteria peut être null
		Assertion.checkNotNull(dtListFilter);
		//-----

		mdlUriFilterMap.put(uri, dtListFilter);

		if (!defaultMdlMap2.containsKey(uri.getDtDefinition())) {
			//On n'insère que le premier considérée par défaut
			defaultMdlMap2.put(uri.getDtDefinition(), uri);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsMasterData(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		return defaultMdlMap2.containsKey(dtDefinition);
	}

	/** {@inheritDoc} */
	@Override
	public DtListURIForMasterData getDtListURIForMasterData(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		final DtListURIForMasterData uri = defaultMdlMap2.get(dtDefinition);
		//		final MasterDataDefinition masterDataDefinition = masterDataDefinitionMap.get(dtDefinition);
		//		return getDomainManager().getDomainFactory().createDtListURI(masterDataDefinition, null);//pas de code : on prend celle par défaut
		return uri;
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor getFilter(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtListProcessor function = mdlUriFilterMap.get(uri);
		return function != null ? function : identityFunction;
	}
}
