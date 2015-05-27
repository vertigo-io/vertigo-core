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
package io.vertigo.dynamo.search.metamodel;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.lang.Assertion;

/**
 * Définition de l'index de recherche.
 *
 * Fondalementalement un index est constitué de deux types d'objets.
 * - Un objet d'index (les champs indexés)
 * - Un subject représentant le concept métier réprésenté par cet index.
 * La définition d'index précise également un SearchLoader permettant la mise à jour autonome de l'index.
 *
 * L'objet d'index est à la fois porteur des champs de recherche, et ceux utilisé à l'affichage.
 * La différence entre les deux peut-être affiné par :
 * - la propriété 'persistent' des fields pour savoir si le champs fait partit ou non du résultat utilisé pour l'affichage
 * - le domain et sa propriété indexType pour savoir si le champs est indéxé ou non
 *
 * L'objet d'affichage peut être simple (Ex: résultat google) alors qu'il se réfère à un index plus riche.
 *
 * @author dchallas, npiedeloup
 */
@DefinitionPrefix("IDX")
public final class SearchIndexDefinition implements Definition {
	/**
	* Nom de l'index.
	*/
	private final String name;

	/** Structure des éléments indexés. */
	private final DtDefinition indexDtDefinition;

	private final DtDefinition subjectDtDefinition;

	private final String searchLoaderId;

	/**
	 * Constructeur.
	 * @param name Index name
	 * @param subjectDtDefinition Subject associé à l'index
	 * @param indexDtDefinition Structure des éléments indexés.
	 * @param searchLoaderId Loader de chargement des éléments indéxés et résultat
	 */
	public SearchIndexDefinition(final String name,
			final DtDefinition subjectDtDefinition,
			final DtDefinition indexDtDefinition,
			final String searchLoaderId) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(subjectDtDefinition);
		Assertion.checkArgument(subjectDtDefinition.getStereotype() == DtStereotype.Subject, "subjectDtDefinition ({0}) must be a DtDefinition of a DtSubject class", subjectDtDefinition.getName());
		Assertion.checkNotNull(indexDtDefinition);
		Assertion.checkArgNotEmpty(searchLoaderId);
		//-----
		this.name = name;
		this.subjectDtDefinition = subjectDtDefinition;
		this.indexDtDefinition = indexDtDefinition;
		this.searchLoaderId = searchLoaderId;
	}

	/**
	 * Définition de l'objet représentant le contenu de l'index (indexé et résultat).
	 * @return Définition des champs indexés.
	 */
	public DtDefinition getIndexDtDefinition() {
		return indexDtDefinition;
	}

	/**
	 * Définition du subject maitre de cet index.
	 * Le Subject de l'index est surveillé pour rafraichir l'index.
	 * @return Définition du subject.
	 */
	public DtDefinition getSubjectDtDefinition() {
		return subjectDtDefinition;
	}

	/**
	 * Nom du composant de chargement des éléments à indexer.
	 * @return Nom du composant de chargement des éléments à indexer.
	 */
	public String getSearchLoaderId() {
		return searchLoaderId;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
