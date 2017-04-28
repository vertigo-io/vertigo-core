/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
import java.util.function.Predicate;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.store.datastore.MasterDataConfig;
import io.vertigo.lang.Assertion;

/**
 * Configuration des listes de référence.
 * @author pchretien
 */
public final class MasterDataConfigImpl implements MasterDataConfig {

	/** CollectionsManager.*/
	private final CollectionsManager collectionsManager;

	private final Map<DtListURIForMasterData, Predicate> mdlUriFilterMap = new HashMap<>();
	private final Map<DtDefinition, DtListURIForMasterData> defaultMdlMap2 = new HashMap<>();

	/**
	 * Constructor.
	 * @param collectionsManager Manager des collections
	 */
	public MasterDataConfigImpl(final CollectionsManager collectionsManager) {
		Assertion.checkNotNull(collectionsManager);
		//-----
		this.collectionsManager = collectionsManager;
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri, final String fieldName, final Serializable value) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(fieldName);
		//-----
		register(uri, collectionsManager.filterByValue(fieldName, value));
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri, final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(fieldName1);
		Assertion.checkNotNull(fieldName2);
		//-----
		final Predicate filter1 = collectionsManager.filterByValue(fieldName1, value1);
		final Predicate filter2 = collectionsManager.filterByValue(fieldName2, value2);
		final Predicate predicate = filter1.and(filter2);
		register(uri, predicate);
	}

	/** {@inheritDoc} */
	@Override
	public void register(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		//-----
		register(uri, o -> true);
	}

	private void register(final DtListURIForMasterData uri, final Predicate dtListFilter) {
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
	public Predicate getFilter(final DtListURIForMasterData uri) {
		Assertion.checkNotNull(uri);
		//-----
		final Predicate predicate = mdlUriFilterMap.get(uri);
		return predicate != null ? predicate : x -> true;
	}
}
