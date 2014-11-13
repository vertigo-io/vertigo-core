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
package io.vertigo.dynamo.plugins.environment.registries;

import io.vertigo.dynamo.impl.environment.DynamicRegistryPlugin;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public abstract class AbstractDynamicRegistryPlugin implements DynamicRegistryPlugin {
	private final Grammar grammar;

	/**
	 * Constructeur.
	 * @param grammar Grammaire
	 */
	protected AbstractDynamicRegistryPlugin(final Grammar grammar) {
		Assertion.checkNotNull(grammar);
		//---------------------------------------------------------------------
		this.grammar = grammar;
	}

	@Override
	public Grammar getGrammar() {
		return grammar;
	}

	/** {@inheritDoc} */
	@Override
	public void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository) {
		//Par défaut rien .
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param property Propriété
	 * @return Propriété de type Boolean uniquement
	 */
	protected static final Boolean getPropertyValueAsBoolean(final DynamicDefinition xdefinition, final EntityProperty property) {
		return (Boolean) xdefinition.getPropertyValue(property);
	}

	/**
	 * Raccourci vers un accesseur typé du PropertyContainer.
	 * Cette méthode retourne la même valeur que getPropertyValue() !
	 * @param property Propriété
	 * @return Propriété de type String uniquement
	 */
	protected static final String getPropertyValueAsString(final DynamicDefinition xdefinition, final EntityProperty property) {
		return (String) xdefinition.getPropertyValue(property);
	}

}
