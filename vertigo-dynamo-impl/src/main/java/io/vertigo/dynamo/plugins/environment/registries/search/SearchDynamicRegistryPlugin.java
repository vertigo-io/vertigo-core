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
package io.vertigo.dynamo.plugins.environment.registries.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;

/**
 * @author pchretien
 */
public final class SearchDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {

	public SearchDynamicRegistryPlugin() {
		super(SearchGrammar.GRAMMAR);
		Home.getDefinitionSpace().register(SearchIndexDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public void onDefinition(final DynamicDefinition xdefinition) {
		if (SearchGrammar.INDEX_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			// Seuls les controllers sont gérés.
			final SearchIndexDefinition indexDefinition = createIndexDefinition(xdefinition);
			Home.getDefinitionSpace().put(indexDefinition, SearchIndexDefinition.class);
		}
	}

	private static SearchIndexDefinition createIndexDefinition(final DynamicDefinition xsearchObjet) {
		final DtDefinition indexDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtIndex").getName(), DtDefinition.class);
		final DtDefinition resultDtDefinition = Home.getDefinitionSpace().resolve(xsearchObjet.getDefinitionKey("dtResult").getName(), DtDefinition.class);
		//	final List<FacetDefinition> facetDefinitions = Collections.emptyList();
		final String definitionName = xsearchObjet.getDefinitionKey().getName();

		final SearchIndexDefinition indexDefinition = new SearchIndexDefinition(definitionName, indexDtDefinition, resultDtDefinition);
		//indexDefinition.makeUnmodifiable();
		return indexDefinition;
	}
}
