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
package io.vertigo.studio.plugins.mda.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileConfig;
import io.vertigo.util.MapBuilder;

import java.util.Map;

/**
 * Génération des objets relatifs au module Search.
 *
 * @author dchallas
 */
public final class SearchGeneratorPlugin extends AbstractGeneratorPlugin {

	/** {@inheritDoc}  */
	@Override
	public void generate(final FileConfig searchConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(searchConfiguration);
		Assertion.checkNotNull(resultBuilder);
		//-----
		generateDtDefinitions(searchConfiguration, resultBuilder);
	}

	private static void generateDtDefinitions(final FileConfig searchConfig, final ResultBuilder resultBuilder) {
		for (final SearchIndexDefinition indexDefinition : Home.getDefinitionSpace().getAll(SearchIndexDefinition.class)) {
			generateSchema(searchConfig, resultBuilder, indexDefinition);
		}
	}

	private static void generateSchema(final FileConfig searchConfiguration, final ResultBuilder resultBuilder, final SearchIndexDefinition indexDefinition) {
		/** Registry */
		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("indexDefinition", indexDefinition)
				.put("indexType", new TemplateMethodIndexType())
				.build();

		createFileGenerator(searchConfiguration, mapRoot, "schema", "solr/" + indexDefinition.getName() + "/conf", ".xml", "search/schema.ftl")
				.generateFile(resultBuilder);
	}
}
