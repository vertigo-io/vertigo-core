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
package io.vertigo.dynamo.impl.persistence.util;

import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.task.TaskManager;

/**
 * Classe utilitaire pour accéder au Broker.
 *
 * @author npiedeloup
 * @param <D> Type d'objet métier.
 * @param <P> Type de la clef primaire.
 */
public class SubjectDAOBroker<D extends DtSubject, P> extends DAOBroker<D, P> {

	private final SearchManager searchManager;

	/**
	 * Contructeur.
	 *
	 * @param dtObjectClass Définition du DtObject associé à ce DAOBroker
	 * @param persistenceManager Manager de gestion de la persistance
	 * @param taskManager Manager de gestion des tâches
	 */
	public SubjectDAOBroker(final Class<? extends DtObject> dtObjectClass, final PersistenceManager persistenceManager, final TaskManager taskManager, final SearchManager searchManager) {
		super(DtObjectUtil.findDtDefinition(dtObjectClass), persistenceManager, taskManager);
		this.searchManager = searchManager;
	}

	public void workOnSubject(final URI<D> uri) {
		broker.workOn(uri);
	}

	public void workOnSubject(final P id) {
		workOnSubject(createDtObjectURI(id));
	}

	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param indexDefinition Type de l'index
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête
	 * @param <R> Type de l'objet resultant de la recherche
	 */
	public <R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(indexDefinition, searchQuery, listState);
	}

}
