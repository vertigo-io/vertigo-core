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
package io.vertigo.dynamo.search;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.lang.Component;

import java.util.Collection;
import java.util.List;

/**
 * Gestionnaire des indexes de recherche.
 *
 * @author dchallas, npiedeloup
 */
public interface SearchManager extends Component {

	/**
	 * Find IndexDefinition for a DtSubject. It must be one and only one IndexDefinition.
	 * @param dtSubjectClass DtSubject class
	 * @return SearchIndexDefinition for this DtSubject (not null)
	 */
	//TODO si par DtDefinition comment s'assurer que c'est un subject ?
	SearchIndexDefinition findIndexDefinitionBySubject(Class<? extends DtSubject> dtSubjectClass);

	/**
	 * Check if a DtSubject have an IndexDefinition.
	 * @param dtSubjectClass DtSubject class
	 * @return if there is a IndexDefinition for this Subject
	 */
	boolean hasIndexDefinitionBySubject(Class<? extends DtSubject> dtSubjectClass);

	/**
	 * Mark an uri list as dirty. Index of these elements will be reindexed.
	 * Reindexation isn't syncrhone, strategy is dependant of plugin's parameters.
	 * @param subjectUris Uri of subject marked as dirty.
	 */
	void markAsDirty(List<URI<? extends DtSubject>> subjectUris);

	/**
	 * Launch a complete reindexation of an index.
	 * @param indexDefinition Type de l'index
	 */
	void reindexAll(SearchIndexDefinition indexDefinition);

	/**
	 * Ajout de plusieurs ressources à l'index.
	 * Si les éléments étaient déjà dans l'index ils sont remplacés.
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param indexDefinition Type de l'index
	 * @param indexCollection Liste des objets à pousser dans l'index (I + R)
	 */
	<S extends DtSubject, I extends DtObject, R extends DtObject> void putAll(SearchIndexDefinition indexDefinition, Collection<SearchIndex<S, I, R>> indexCollection);

	/**
	 * Ajout d'une ressource à l'index.
	 * Si l'élément était déjà dans l'index il est remplacé.
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param indexDefinition Type de l'index
	 * @param index Objet à pousser dans l'index (I + R)
	 */
	<S extends DtSubject, I extends DtObject, R extends DtObject> void put(SearchIndexDefinition indexDefinition, SearchIndex<S, I, R> index);

	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param indexDefinition Type de l'index
	 * @param listState Etat de la liste (tri et pagination)
	 * @return Résultat correspondant à la requête
	 * @param <R> Type de l'objet resultant de la recherche
	 */
	<R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(SearchIndexDefinition indexDefinition, final SearchQuery searchQuery, final DtListState listState);

	/**
	 * @param indexDefinition  Type de l'index
	 * @return Nombre de document indexés
	 */
	long count(SearchIndexDefinition indexDefinition);

	/**
	 * Suppression d'une ressource de l'index.
	 * @param indexDefinition Type de l'index
	 * @param uri URI de la ressource à supprimer
	 */
	void remove(SearchIndexDefinition indexDefinition, final URI uri);

	/**
	 * Suppression des données correspondant à un filtre.
	 * @param indexDefinition Type de l'index
	 * @param listFilter Filtre des éléments à supprimer
	 */
	void removeAll(SearchIndexDefinition indexDefinition, final ListFilter listFilter);

}
