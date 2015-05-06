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
package io.vertigo.dynamo.search.model;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.lang.Assertion;

/**
 * Objet d'échange avec l'index.
 * Cet objet permet de
 *  - construire l'index de recherche
 *  - consulter le résultat d'une recherhe
 *
 * @author dchallas
 * @param <I> Type de l'objet contenant les champs à indexer
 * @param <R> Type de l'objet resultant de la recherche
 */
public final class SearchIndex<S extends DtSubject, I extends DtObject, R extends DtObject> {
	/** Définition de l'index. */
	private final SearchIndexDefinition indexDefinition;

	/** URI de l'objet indexé : par convention il s'agit de l'uri de O.*/
	private final URI<S> uri;

	/** DtObject d'index. */
	private final I indexDtObject;

	/** DtObject de resultat. */
	private final R resultDtObject;

	/**
	 * Constructeur .
	 * @param indexDefinition definition de O, I, R
	 * @param uri URI de l'objet indexé
	 */
	private SearchIndex(final SearchIndexDefinition indexDefinition, final URI<S> uri, final I indexDtObject, final R resultDtObject) {
		Assertion.checkNotNull(uri);
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(resultDtObject);
		//indexDtObject peut être null
		//On vérifie la consistance des données.
		Assertion.checkArgument(indexDefinition.getResultDtDefinition().equals(DtObjectUtil.findDtDefinition(resultDtObject)), "le type du DTO result n''est pas correct");
		Assertion.checkArgument(indexDtObject == null || indexDefinition.getIndexDtDefinition().equals(DtObjectUtil.findDtDefinition(indexDtObject)), "le type du DTO index n''est pas correct");
		//-----
		this.uri = uri;
		this.indexDefinition = indexDefinition;
		this.indexDtObject = indexDtObject;
		this.resultDtObject = resultDtObject;
	}

	/**
	 * @return Définition de l'index.
	 */
	public SearchIndexDefinition getDefinition() {
		return indexDefinition;
	}

	/**
	 * Récupération de l'objet de résultat.
	 * @return Objet de résultat de résultat
	 */
	public R getResultDtObject() {
		return resultDtObject;
	}

	/**
	 * Récupération de l'uri de la ressource indexée.
	 *  - Utilisé pour la récupération de highlight.
	 * @return URI de la ressource indexée.
	 */
	public URI<S> getURI() {
		return uri;
	}

	/**
	 * Récupération de l'object contenant les champs à indexer.
	 * @return Objet contenant les champs à indexer
	 */
	public I getIndexDtObject() {
		Assertion.checkArgument(hasIndex(), "Index n'est pas dans l'état indexable.");
		//-----
		return indexDtObject;
	}

	/**
	 * @return Contient l'objet d'index
	 */
	private boolean hasIndex() {
		return indexDtObject != null;
	}

	/**
	 * Constructeur de l'Objet permettant de créer l'index.
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param uri URI de l'objet indexé
	 * @param indexDefinition Définition de l'index de recherche.
	 * @param resultDto DTO représentant le résultat
	 * @param indexDto  DTO représentant l'index
	 * @return  Objet permettant de créer l'index
	 */
	public static <S extends DtSubject, I extends DtObject, R extends DtObject> SearchIndex<S, I, R> createIndex(final SearchIndexDefinition indexDefinition, final URI<S> uri, final I indexDto, final R resultDto) {
		return new SearchIndex<>(indexDefinition, uri, indexDto, resultDto);
	}

	/**
	 * Constructeur de l'objet permettant d'accéder au résultat d'une recherche .
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param uri URI de l'objet indexé
	 * @param indexDefinition Définition de l'index de recherche.
	 * @param resultDto DTO représentant le résultat
	 * @return Objet permettant d'accéder au résultat d'une recherche
	 */
	public static <S extends DtSubject, I extends DtObject, R extends DtObject> SearchIndex<S, I, R> createResult(final SearchIndexDefinition indexDefinition, final URI<S> uri, final R resultDto) {
		return new SearchIndex<>(indexDefinition, uri, null, resultDto);
	}
}
