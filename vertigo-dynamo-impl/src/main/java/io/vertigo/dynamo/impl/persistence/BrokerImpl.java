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
package io.vertigo.dynamo.impl.persistence;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.impl.persistence.cache.CacheDataStore;
import io.vertigo.dynamo.impl.persistence.logical.LogicalFileStore;
import io.vertigo.dynamo.impl.persistence.logical.LogicalDataStore;
import io.vertigo.dynamo.persistence.Broker;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.DataStore;

/**
 * Implémentation Standard du Broker.
 * Cette implémentation s'appuie sur le concept de Store.
 * Un store définit les modalités du stockage 
 * alors que le broker se concentre sur la problématique des accès aux ressources.
 * @author pchretien
 */
final class BrokerImpl implements Broker {
	/** Le store est le point d'accès unique à la base (sql, xml, fichier plat...). */
	private final DataStore dataStore;
	private final FileStore fileStore;

	/**
	 * Constructeur. 
	 * Une fois le broker construit la configuration est bloquée.
	 * @param brokerConfiguration Configuration du broker
	 */
	BrokerImpl(final BrokerConfigurationImpl brokerConfiguration) {
		Assertion.checkNotNull(brokerConfiguration);
		//---------------------------------------------------------------------
		//On vérouille la configuration.
		//brokerConfiguration.lock();
		//On crée la pile de Store.
		final DataStore logicalDataStore = new LogicalDataStore(brokerConfiguration.getLogicalStoreConfiguration(), this);
		dataStore = new CacheDataStore(logicalDataStore, brokerConfiguration.getCacheStoreConfiguration());
		fileStore = new LogicalFileStore(brokerConfiguration.getLogicalFileStoreConfiguration());
	}

	//==========================================================================
	//===================Méthodes publiques du Broker===========================
	//==========================================================================
	/** {@inheritDoc} */
	public void save(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//----------------------------------------------------------------------
		dataStore.put(dto);

	}

	/** {@inheritDoc} */
	public void save(final FileInfo fileInfo) {
		Assertion.checkNotNull(fileInfo);
		//----------------------------------------------------------------------
		fileStore.put(fileInfo);
	}

	/** {@inheritDoc} */
	public void merge(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//----------------------------------------------------------------------
		dataStore.merge(dto);
	}

	/** {@inheritDoc} */
	public void delete(final URI<? extends DtObject> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		dataStore.remove(uri);
	}

	/** {@inheritDoc} */
	public void deleteFileInfo(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		fileStore.remove(uri);
	}

	//==========================================================================
	//==============================Accesseurs =================================
	//==========================================================================
	/** {@inheritDoc} */
	public <D extends DtObject> Option<D> getOption(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		final D dto = dataStore.<D> load(uri);
		//----------------------------------------------------------------------
		return Option.option(dto);
	}

	/** {@inheritDoc} */
	public <D extends DtObject> D get(final URI<D> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		//on ne reutilise pas le getOption volontairement 
		//car c'est ici le cas le plus courant, et on l'optimise au maximum
		final D dto = dataStore.<D> load(uri);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(dto, "L''objet {0} n''a pas été trouvé", uri);
		return dto;
	}

	/** {@inheritDoc} */
	public FileInfo getFileInfo(final URI<FileInfo> uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		final FileInfo fileInfo = fileStore.load(uri);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(fileInfo, "Le fichier {0} n''a pas été trouvé", uri);
		return fileInfo;
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtList<D> getList(final DtListURI uri) {
		Assertion.checkNotNull(uri);
		//----------------------------------------------------------------------
		final DtList<D> dtc = dataStore.loadList(uri);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	@Deprecated
	public <D extends DtObject> DtList<D> getList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		final DtList<D> dtc = dataStore.loadList(dtDefinition, criteria, maxRows);
		//----------------------------------------------------------------------
		Assertion.checkNotNull(dtc);
		return dtc;
	}

	/** {@inheritDoc} */
	public int count(final DtDefinition dtDefinition) {
		return dataStore.count(dtDefinition);
	}
}
