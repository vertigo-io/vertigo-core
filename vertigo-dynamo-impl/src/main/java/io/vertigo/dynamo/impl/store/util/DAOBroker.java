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
package io.vertigo.dynamo.impl.store.util;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.FilterCriteria;
import io.vertigo.dynamo.store.criteria.FilterCriteriaBuilder;
import io.vertigo.dynamo.store.datastore.BrokerBatch;
import io.vertigo.dynamo.store.datastore.BrokerNN;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Classe utilitaire pour accéder au Broker.
 *
 * @author cgodard
 * @param <D> Type d'objet métier.
 * @param <P> Type de la clef primaire.
 */
public class DAOBroker<D extends DtObject, P> implements BrokerNN, BrokerBatch<D, P> {

	/** DT de l'objet dont on gére le CRUD. */
	private final DtDefinition dtDefinition;
	protected final DataStore dataStore;
	private final BrokerNN brokerNN;
	private final BrokerBatch<D, P> brokerBatch;
	private final TaskManager taskManager;

	/**
	 * Contructeur.
	 *
	 * @param dtObjectClass Définition du DtObject associé à ce DAOBroker
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAOBroker(final Class<? extends DtObject> dtObjectClass, final StoreManager storeManager, final TaskManager taskManager) {
		this(DtObjectUtil.findDtDefinition(dtObjectClass), storeManager, taskManager);
	}

	/**
	 * Contructeur.
	 *
	 * @param dtDefinition Définition du DtObject associé à ce DAOBroker
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAOBroker(final DtDefinition dtDefinition, final StoreManager storeManager, final TaskManager taskManager) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(taskManager);
		//-----
		dataStore = storeManager.getDataStore();
		brokerNN = storeManager.getBrokerNN();
		this.dtDefinition = dtDefinition;
		brokerBatch = new BrokerBatchImpl<>(dtDefinition, taskManager);
		this.taskManager = taskManager;
	}

	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Save an object.
	 *
	 * @param dto Object to save
	 */
	public final void save(final D dto) {
		if (DtObjectUtil.getId(dto) == null) {
			dataStore.create(dto);
		} else {
			dataStore.update(dto);
		}
	}

	/**
	 * Create an object.
	 *
	 * @param dto Object to create
	 */
	public final void create(final D dto) {
		dataStore.create(dto);
	}

	/**
	 * Update an object.
	 *
	 * @param dto Object to update
	 */
	public final void update(final D dto) {
		dataStore.update(dto);
	}

	/**
	 * Suppression d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	public final void delete(final URI<D> uri) {
		dataStore.delete(uri);
	}

	/**
	 * Suppression d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant à supprimer
	 */
	public final void delete(final P id) {
		delete(createDtObjectURI(id));
	}

	/**
	 * Récupération d'un objet persistant par son URI. L'objet doit exister.
	 *
	 * @param uri URI de l'objet à récupérer
	 * @return D Object recherché
	 */
	public final D get(final URI<D> uri) {
		return dataStore.<D> get(uri);
	}

	/**
	 * Récupération d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return D Object objet recherché
	 */
	public final D get(final P id) {
		return get(createDtObjectURI(id));
	}

	/**
	 * Retourne l'URI de DtObject correspondant à une URN de définition et une valeur d'URI donnés.
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return URI recherchée
	 */
	protected final URI<D> createDtObjectURI(final P id) {
		return new URI<>(dtDefinition, id);
	}

	/**
	 * @param fieldName de l'object à récupérer NOT NULL
	 * @param value de l'object à récupérer NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<D> getListByDtField(final String fieldName, final Object value, final int maxRows) {
		final FilterCriteria<D> criteria = new FilterCriteriaBuilder<D>().addFilter(fieldName, value).build();
		// Verification de la valeur est du type du champ
		dtDefinition.getField(fieldName).getDomain().getDataType().checkValue(value);
		return dataStore.<D> getList(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/**
	 * @param criteria Critére de recherche NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<D> getList(final Criteria<D> criteria, final int maxRows) {
		return dataStore.<D> getList(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/** {@inheritDoc} */
	@Override
	public final void removeAllNN(final DtListURIForNNAssociation dtListURI) {
		brokerNN.removeAllNN(dtListURI);
	}

	/** {@inheritDoc} */
	@Override
	public final void removeNN(final DtListURIForNNAssociation dtListURI, final URI uriToDelete) {
		brokerNN.removeNN(dtListURI, uriToDelete);
	}

	/**
	 * Mise à jour des associations n-n.
	 *
	 * @param <FK> <FK extends DtObject>
	 * @param dtListURI DtList de référence
	 * @param newDtc DtList modifiée
	 */
	public final <FK extends DtObject> void updateNN(final DtListURIForNNAssociation dtListURI, final DtList<FK> newDtc) {
		Assertion.checkNotNull(newDtc);
		//-----
		final List<URI> objectURIs = new ArrayList<>();
		for (final FK dto : newDtc) {
			objectURIs.add(createURI(dto));
		}
		updateNN(dtListURI, objectURIs);
	}

	/** {@inheritDoc} */
	@Override
	public final void updateNN(final DtListURIForNNAssociation dtListURI, final List<URI> newUriList) {
		brokerNN.updateNN(dtListURI, newUriList);
	}

	/** {@inheritDoc} */
	@Override
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final URI uriToAppend) {
		brokerNN.appendNN(dtListURI, uriToAppend);
	}

	/**
	 * Ajout un objet à la collection existante.
	 *
	 * @param dtListURI DtList de référence
	 * @param dtoToAppend Objet à ajout à la NN
	 */
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final DtObject dtoToAppend) {
		brokerNN.appendNN(dtListURI, createURI(dtoToAppend));
	}

	private static <D extends DtObject> URI<D> createURI(final D dto) {
		Assertion.checkNotNull(dto);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
		return new URI<>(dtDefinition, DtObjectUtil.getId(dto));
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> getList(final Collection<P> idList) {
		return brokerBatch.getList(idList);
	}

	/** {@inheritDoc} */
	@Override
	public Map<P, D> getMap(final Collection<P> idList) {
		return brokerBatch.getMap(idList);
	}

	/** {@inheritDoc} */
	@Override
	public <O> DtList<D> getListByField(final String fieldName, final Collection<O> value) {
		return brokerBatch.getListByField(fieldName, value);
	}

	/** {@inheritDoc} */
	@Override
	public <O> Map<O, DtList<D>> getMapByField(final String fieldName, final Collection<O> value) {
		return brokerBatch.getMapByField(fieldName, value);
	}
}
