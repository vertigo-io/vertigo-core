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
package io.vertigo.dynamo.collections.metamodel;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.metamodel.Definition;
import io.vertigo.core.stereotype.Prefix;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition des requêtes d'accès à l'index de recherche.
 * 
 * les requêtes sont facettées.
 *
 * @author pchretien 
 */
@Prefix("QRY")
public final class FacetedQueryDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;

	/** Liste indexée des facettes.*/
	private final Map<String, FacetDefinition> facetDefinitions = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param facetDefinitions Liste des facettes
	 */
	public FacetedQueryDefinition(final String name, final List<FacetDefinition> facetDefinitions) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(facetDefinitions);
		//---------------------------------------------------------------------
		this.name = name;
		for (final FacetDefinition facetDefinition : facetDefinitions) {
			this.facetDefinitions.put(facetDefinition.getName(), facetDefinition);
		}
	}

	/**
	 * Retourne la facette identifié par son nom.
	 *
	 * @param facetName Nom de la facette recherché.
	 * @return Définition de la facette.
	 */
	public FacetDefinition getFacetDefinition(final String facetName) {
		Assertion.checkArgNotEmpty(facetName);
		//---------------------------------------------------------------------
		final FacetDefinition facetDefinition = facetDefinitions.get(facetName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(facetDefinition, "Aucune Définition de facette trouvée pour {0}", facetName);
		return facetDefinition;
	}

	/**
	 * @return Liste des facettes portées par l'index.
	 */
	public Collection<FacetDefinition> getFacetDefinitions() {
		return Collections.<FacetDefinition> unmodifiableCollection(facetDefinitions.values());
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
