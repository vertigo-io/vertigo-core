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
package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.definition.dsl.dynamic.DynamicRegistry;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * Mock pour les tests de regles sur les Definitions.
 * @author npiedeloup
 */
public final class DslDynamicRegistryMock implements DynamicRegistry {

	private DslDynamicRegistryMock() {
		//constructeur private
	}

	/**
	 * @return DynamicDefinitionRepository bouchon pour test
	 */
	public static DynamicDefinitionRepository createDynamicDefinitionRepository() {
		return new DynamicDefinitionRepository(new DslDynamicRegistryMock());
	}

	@Override
	public EntityGrammar getGrammar() {
		return new DomainGrammar();
	}

	@Override
	public Definition createDefinition(final DefinitionSpace definitionSpace, final DynamicDefinition definition) {
		return new Definition() {

			@Override
			public String getName() {
				return "FAKE";
			}
		};
	}

	@Override
	public List<DynamicDefinition> getRootDynamicDefinitions() {
		return Collections.emptyList();
	}

	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//
	}

}
