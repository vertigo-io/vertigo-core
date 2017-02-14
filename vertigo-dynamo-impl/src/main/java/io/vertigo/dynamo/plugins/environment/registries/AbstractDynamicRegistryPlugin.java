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
package io.vertigo.dynamo.plugins.environment.registries;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.dynamic.DslDefinitionRepository;
import io.vertigo.core.definition.dsl.entity.DslGrammar;
import io.vertigo.core.definition.loader.DynamicRegistryPlugin;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public abstract class AbstractDynamicRegistryPlugin implements DynamicRegistryPlugin {
	private final DslGrammar grammar;

	/**
	 * Constructeur.
	 * @param grammar Grammaire
	 */
	protected AbstractDynamicRegistryPlugin(final DslGrammar grammar) {
		Assertion.checkNotNull(grammar);
		//-----
		this.grammar = grammar;
	}

	@Override
	public DslGrammar getGrammar() {
		return grammar;
	}

	@Override
	public List<DslDefinition> getRootDynamicDefinitions() {
		return Collections.emptyList();
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param propertyName Name of the property
	 * @return Propriété de type Boolean uniquement
	 */
	protected static final Boolean getPropertyValueAsBoolean(final DslDefinition xdefinition, final String propertyName) {
		return (Boolean) xdefinition.getPropertyValue(propertyName);
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param propertyName Name of the property
	 * @return Propriété de type String uniquement
	 */
	protected static final String getPropertyValueAsString(final DslDefinition xdefinition, final String propertyName) {
		return (String) xdefinition.getPropertyValue(propertyName);
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DslDefinition xdefinition, final DslDefinitionRepository dynamicModelrepository) {
		//
	}

}
