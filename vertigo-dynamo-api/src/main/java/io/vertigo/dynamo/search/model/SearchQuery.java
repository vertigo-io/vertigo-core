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

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.lang.Assertion;

import java.io.Serializable;

/**
 * Critères de recherche.
 * @author npiedeloup
 */
public final class SearchQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final String indexDefinitionName;
	private final ListFilter listFilter;
	//Informations optionnelles pour trier les résultats (null si inutilisé => tri par pertinence)
	private final String sortFieldName;
	private final Boolean sortAsc; //true si tri ascendant 

	//Informations optionnelles pour booster la pertinence des documents plus récent (null si inutilisé)
	private final String boostedDocumentDateFieldName;
	private final Integer numDaysOfBoostRefDocument;
	private final Integer mostRecentBoost;

	/**
	 * Constructeur.
	 * @param indexDefinition IndexDefinition de la requête
	 * @param listFilter Filtre principal correspondant aux critères de la recherche
	 * @param sortField Nom du champ utilisé pour le tri (null si non utilisé)
	 * @param sortAsc Ordre de tri, True si ascendant (null si non utilisé)
	 * @param boostedDocumentDateField Nom du champ portant la date du document (null si non utilisé)
	 * @param numDaysOfBoostRefDocument Age des documents servant de référence pour le boost des plus récents par rapport à eux (null si non utilisé)
	 * @param mostRecentBoost Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1) (null si non utilisé)
	 */
	private SearchQuery(final IndexDefinition indexDefinition, final ListFilter listFilter, final DtField sortField, final Boolean sortAsc, final DtField boostedDocumentDateField, final Integer numDaysOfBoostRefDocument, final Integer mostRecentBoost) {
		Assertion.checkNotNull(indexDefinition);
		Assertion.checkNotNull(listFilter);
		Assertion.checkArgument(sortField == null || sortAsc != null, "Lorsque le tri des documents est activé, sortAsc est obligatoires.");
		Assertion.checkArgument(boostedDocumentDateField == null || numDaysOfBoostRefDocument != null && mostRecentBoost != null, "Lorsque le boost des documents récents est activé, numDaysOfBoostRefDocument et mostRecentBoost sont obligatoires.");
		Assertion.checkArgument(boostedDocumentDateField != null || numDaysOfBoostRefDocument == null && mostRecentBoost == null, "Lorsque le boost des documents récents est désactivé, numDaysOfBoostRefDocument et mostRecentBoost doivent être null.");
		Assertion.checkArgument(numDaysOfBoostRefDocument == null || mostRecentBoost == null || numDaysOfBoostRefDocument.longValue() > 1 && mostRecentBoost.longValue() > 1, "numDaysOfBoostRefDocument et mostRecentBoost doivent être strictement supérieur à 1.");
		//---------------------------------------------------------------------
		indexDefinitionName = indexDefinition.getName();
		this.listFilter = listFilter;
		sortFieldName = sortField != null ? sortField.getName() : null;
		this.sortAsc = sortAsc;
		boostedDocumentDateFieldName = boostedDocumentDateField != null ? boostedDocumentDateField.getName() : null;
		this.numDaysOfBoostRefDocument = numDaysOfBoostRefDocument;
		this.mostRecentBoost = mostRecentBoost;
	}

	/**
	 * @return Index sur lequel porte la recherche
	 */
	public IndexDefinition getIndexDefinition() {
		return Home.getDefinitionSpace().resolve(indexDefinitionName, IndexDefinition.class);
	}

	/**
	 * Filtre principal correspondant aux critères de la recherche.
	 * @return Valeur du filtre
	 */
	public ListFilter getListFilter() {
		return listFilter;
	}

	/**
	 * Indique que les documents doivent être trié par un champs particulier.
	 *@return si le tri est activé
	 */
	public boolean isSortActive() {
		return sortFieldName != null;
	}

	/**
	 * Si le tri des documents est activé.
	 * @return Nom du champ portant la date du document
	 */
	public String getSortField() {
		Assertion.checkArgument(isSortActive(), "Le tri des documents n'est pas activé sur cette recherche");
		//---------------------------------------------------------------------
		return sortFieldName;
	}

	/**
	 * Si le tri des documents est activé.
	 * @return Age des documents servant de référence pour le boost des plus récents par rapport à eux
	 */
	public boolean getSortAsc() {
		Assertion.checkArgument(isSortActive(), "Le tri des documents n'est pas activé sur cette recherche");
		//---------------------------------------------------------------------
		return sortAsc;
	}

	/**
	 * Indique que la recherche boost les documents les plus récents.
	 * C'est une formule de type 1/x qui est utilisée.
	 * La formule de boost est 1 / ((documentAgeDay/NumDaysOfBoostRefDocument) + (1/(MostRecentBoost-1)))
	 * @return si le boost est activé
	 */
	public boolean isBoostMostRecent() {
		return boostedDocumentDateFieldName != null;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Nom du champ portant la date du document
	 */
	public String getBoostedDocumentDateField() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//---------------------------------------------------------------------
		return boostedDocumentDateFieldName;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Age des documents servant de référence pour le boost des plus récents par rapport à eux
	 */
	public int getNumDaysOfBoostRefDocument() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//---------------------------------------------------------------------
		return numDaysOfBoostRefDocument;
	}

	/**
	 * Si le booste des documents recents est activé.
	 * @return Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1).
	 */
	public int getMostRecentBoost() {
		Assertion.checkArgument(isBoostMostRecent(), "Le boost des documents les plus récent, n'est pas activé sur cette recherche");
		//---------------------------------------------------------------------
		return mostRecentBoost;
	}

	//=========================================================================
	//=======================Factory des Queries===============================
	//=========================================================================
	/**
	 * Crée un SearchQuery.
	 * @param indexDefinition definition de l'index
	 * @param listFilter critère principal
	 * @return SearchQuery.
	 */
	public static SearchQuery createSearchQuery(final IndexDefinition indexDefinition, final ListFilter listFilter) {
		return new SearchQuery(indexDefinition, listFilter, null, null, null, null, null);
	}

	/**
	 * Crée un SearchQuery.
	 * @param indexDefinition definition de l'index
	 * @param listFilter critère principal
	 * @param sortField Champ utilisé pour le tri
	 * @param sortAsc  Ordre de tri (true pour ascendant)
	 * @return SearchQuery.
	 */
	public static SearchQuery createSearchQuery(final IndexDefinition indexDefinition, final ListFilter listFilter, final DtField sortField, final boolean sortAsc) {
		return new SearchQuery(indexDefinition, listFilter, sortField, sortAsc, null, null, null);
	}

	/**
	 * Crée un SearchQuery avec boost des documents les plus récents.
	 * @param indexDefinition definition de l'index
	 * @param listFilter critère principal
	 * @param boostedDocumentDateField Nom du champ portant la date du document (null si non utilisé)
	 * @param numDaysOfBoostRefDocument Age des documents servant de référence pour le boost des plus récents par rapport à eux (null si non utilisé)
	 * @param mostRecentBoost Boost relatif maximum entre les plus récents et ceux ayant l'age de référence (doit être > 1) (null si non utilisé)
	 * @return SearchQuery.
	 */
	public static SearchQuery createSearchQuery(final IndexDefinition indexDefinition, final ListFilter listFilter, final DtField boostedDocumentDateField, final Integer numDaysOfBoostRefDocument, final Integer mostRecentBoost) {
		Assertion.checkNotNull(boostedDocumentDateField);
		//---------------------------------------------------------------------
		return new SearchQuery(indexDefinition, listFilter, null, null, boostedDocumentDateField, numDaysOfBoostRefDocument, mostRecentBoost);
	}
}
