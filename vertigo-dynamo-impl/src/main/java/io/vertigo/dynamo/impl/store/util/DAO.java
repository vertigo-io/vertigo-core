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
package io.vertigo.dynamo.impl.store.util;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.FilterCriteria;
import io.vertigo.dynamo.store.criteria.FilterCriteriaBuilder;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.lang.Assertion;

/**
 * Classe utilitaire pour accéder au Broker.
 *
 * @author cgodard
 * @param <E> the type of entity
 * @param <P> Type de la clef primaire.
 */
public class DAO<E extends Entity, P> implements BrokerNN {

	/** DT de l'objet dont on gére le CRUD. */
	private final DtDefinition dtDefinition;
	protected final DataStore dataStore;
	private final BrokerNN brokerNN;
	private final BrokerBatch<E, P> brokerBatch;
	private final TaskManager taskManager;

	/**
	 * Contructeur.
	 *
	 * @param entityClass Définition du DtObject associé à ce DAO
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAO(final Class<? extends Entity> entityClass, final StoreManager storeManager, final TaskManager taskManager) {
		this(DtObjectUtil.findDtDefinition(entityClass), storeManager, taskManager);
	}

	/**
	 * Contructeur.
	 *
	 * @param dtDefinition Définition du DtObject associé à ce DAO
	 * @param storeManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public DAO(final DtDefinition dtDefinition, final StoreManager storeManager, final TaskManager taskManager) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(taskManager);
		//-----
		dataStore = storeManager.getDataStore();
		brokerNN = new BrokerNNImpl(taskManager);
		this.dtDefinition = dtDefinition;
		brokerBatch = new BrokerBatchImpl<>(taskManager);
		this.taskManager = taskManager;
	}

	protected final TaskManager getTaskManager() {
		return taskManager;
	}

	public BrokerBatch<E, P> getBatch() {
		return brokerBatch;
	}

	/**
	 * Save an object.
	 *
	 * @param dto Object to save
	 */
	public final void save(final E dto) {
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
	public final void create(final E dto) {
		dataStore.create(dto);
	}

	/**
	 * Update an object.
	 *
	 * @param dto Object to update
	 */
	public final void update(final E dto) {
		dataStore.update(dto);
	}

	/**
	 * Suppression d'un objet persistant par son URI.
	 *
	 * @param uri URI de l'objet à supprimer
	 */
	public final void delete(final URI<E> uri) {
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
	public final E get(final URI<E> uri) {
		return dataStore.<E> read(uri);
	}

	/**
	 * Récupération d'un objet persistant par son identifiant.<br>
	 * Cette méthode est utile uniquement dans les cas où l'identifiant est un identifiant technique (ex: entier calculé
	 * via une séquence).
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return D Object objet recherché
	 */
	public final E get(final P id) {
		return get(createDtObjectURI(id));
	}

	/**
	 * Retourne l'URI de DtObject correspondant à une URN de définition et une valeur d'URI donnés.
	 *
	 * @param id identifiant de l'objet persistant recherché
	 * @return URI recherchée
	 */
	protected final URI<E> createDtObjectURI(final P id) {
		return new URI<>(dtDefinition, id);
	}

	/**
	 * @param fieldName de l'object à récupérer NOT NULL
	 * @param value de l'object à récupérer NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<E> getListByDtField(final String fieldName, final Object value, final int maxRows) {
		final FilterCriteria<E> criteria = new FilterCriteriaBuilder<E>().addFilter(fieldName, value).build();
		// Verification de la valeur est du type du champ
		dtDefinition.getField(fieldName).getDomain().getDataType().checkValue(value);
		return dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
	}

	/**
	 * @param criteria Critére de recherche NOT NULL
	 * @param maxRows Nombre maximum de ligne
	 * @return DtList<D> récupéré NOT NUL
	 */
	public final DtList<E> getList(final Criteria<E> criteria, final int maxRows) {
		return dataStore.<E> findAll(new DtListURIForCriteria<>(dtDefinition, criteria, maxRows));
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
	public final <FK extends Entity> void updateNN(final DtListURIForNNAssociation dtListURI, final DtList<FK> newDtc) {
		Assertion.checkNotNull(newDtc);
		//-----
		final List<URI> objectURIs = new ArrayList<>();
		for (final FK dto : newDtc) {
			objectURIs.add(DtObjectUtil.createURI(dto));
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
	 * @param entity the entity to append
	 */
	public final void appendNN(final DtListURIForNNAssociation dtListURI, final Entity entity) {
		brokerNN.appendNN(dtListURI, DtObjectUtil.createURI(entity));
	}
}
