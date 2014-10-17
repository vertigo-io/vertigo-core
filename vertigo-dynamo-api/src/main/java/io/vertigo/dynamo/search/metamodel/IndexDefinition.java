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
import io.vertigo.lang.Assertion;

/**
 * Définition de l'index de recherche.
 * 
 * Un index est constitué de deux types d'objets.
 * - Un objet d'index (les champs indexés)
 * - Un objet d'affichage 
 * 
 * L'objet d'affichage peut être simple (Ex: résultat google) alors qu'il se réfère à un index plus riche.
 *  
 * @author dchallas
 */
@DefinitionPrefix("IDX")
public final class IndexDefinition implements Definition {
	/**
	* Nom de l'index.
	*/
	private final String name;

	/** Structure des éléments indexés. */
	private final DtDefinition indexDtDefinition;

	/** Structure des éléments de résultat.*/
	private final DtDefinition resultDtDefinition;

	/**
	 * Constructeur.
	 * @param indexDtDefinition Structure des éléments indexés.
	 * @param resultDtDefinition Structure des éléments de résultat.
	 */
	public IndexDefinition(final String name, final DtDefinition indexDtDefinition, final DtDefinition resultDtDefinition) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(indexDtDefinition);
		Assertion.checkNotNull(resultDtDefinition);
		//---------------------------------------------------------------------
		this.name = name;
		this.indexDtDefinition = indexDtDefinition;
		this.resultDtDefinition = resultDtDefinition;
	}

	/**
	 * Définition des champs indexés.
	 * @return Définition des champs indexés.
	 */
	public DtDefinition getIndexDtDefinition() {
		return indexDtDefinition;
	}

	/**
	 * Définition des éléments résultats.
	 * Les éléments de résultats doivent être conservés, stockés dans l'index.
	 * @return Définition des éléments de résultats.
	 */
	public DtDefinition getResultDtDefinition() {
		return resultDtDefinition;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
