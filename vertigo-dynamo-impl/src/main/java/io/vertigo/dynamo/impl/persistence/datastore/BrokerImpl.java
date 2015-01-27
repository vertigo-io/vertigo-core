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
package io.vertigo.dynamo.impl.persistence.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.persistence.datastore.cache.CacheDataStore;
import io.vertigo.dynamo.impl.persistence.datastore.logical.LogicalDataStore;
import io.vertigo.dynamo.persistence.criteria.Criteria;
import io.vertigo.dynamo.persistence.datastore.Broker;
import io.vertigo.dynamo.persistence.datastore.DataStore;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Implémentation Standard du Broker.
 * Cette implémentation s'appuie sur le concept de Store.
 * Un store définit les modalités du stockage
 * alors que le broker se concentre sur la problématique des accès aux ressources.
 * @author pchretien
 */
public final class BrokerImpl implements Broker {
	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final DataStore dataStore;

	/**
	 * Constructeur.
	 * Une fois le broker construit la configuration est bloquée.
	 * @param brokerConfiguration Configuration du broker
	 */
	public BrokerImpl(final BrokerConfigurationImpl brokerConfiguration) {
		Assertion.checkNotNull(brokerConfiguration);
		//-----
		//On vérouille la configuration.
		//brokerConfiguration.lock();
		//On crée la pile de Store.
		final DataStore logicalDataStore = new LogicalDataStore(brokerConfiguration.getLogicalStoreConfiguration(), this);
		dataStore = new CacheDataStore(logicalDataStore, brokerConfiguration.getCacheStoreConfiguration());
	}

	//==========================================================================
	//===================Méthodes publiques du Broker===========================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public void create(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		dataStore.create(dto);

	}

	/** {@inheritDoc} */
	@Override
	public void update(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		dataStore.update(dto);

	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//-----
		dataStore.merge(dto);
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//-----
		dataStore.remove(uri);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> Option<D> getOption(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		final D dto = dataStore.<D> load(uri);
		//-----
		return Option.option(dto);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> D get(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//-----
		//on ne reutilise pas le getOption volontairement
		//car c'est ici le cas le plus courant, et on l'optimise au maximum
		final D dto = dataStore.<D> load(uri);
		//-----
		Assertion.checkNotNull(dto, "L''objet {0} n''a pas été trouvé", uri);
		return dto;
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> getList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//-----
		final DtList<D> dtc = dataStore.loadList(uri);
		//-----
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	@Deprecated
	@Override
	public <D extends DtObject> DtList<D> getList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		final DtList<D> dtc = dataStore.loadList(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
		//-----
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return dataStore.count(dtDefinition);
	}
}
