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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;

import java.util.List;

/**
 * Une entrée de type définition est définie (XOR) 
 * - soit par un champ et une définition, 
 * - soit par un champ et une liste de clés de définition.
 * 
 * @author pchretien
 */
public final class DslDefinitionEntry {
	/**
	 * Obligatoire
	 */
	private final String fieldName;
	private final DynamicDefinition definition;
	private final List<String> definitionKeys;

	/**
	 * Constructeur.
	 * 
	 * @param definitionKeyList Liste des clés de définition
	 * @param fieldName Nom du champ
	 */
	public DslDefinitionEntry(final String fieldName, final List<String> definitionKeyList) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definitionKeyList);
		// ----------------------------------------------------------------------
		this.fieldName = fieldName;
		definition = null;
		definitionKeys = definitionKeyList;
	}

	/**
	 * Constructeur.
	 * 
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	public DslDefinitionEntry(final String fieldName, final DynamicDefinition definition) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definition);
		// ----------------------------------------------------------------------
		this.fieldName = fieldName;
		this.definition = definition;
		definitionKeys = null;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Permet de savoir dans quel mode on se trouve (Definition XOR List<DefinitionKey>).
	 * 
	 * @return boolean
	 */
	public boolean containsDefinition() {
		return definition != null;
	}

	/**
	 * @return Définition
	 */
	public DynamicDefinition getDefinition() {
		Assertion.checkNotNull(definition);
		// ----------------------------------------------------------------------
		return definition;
	}

	/**
	 * @return List des clés de définition
	 */
	public List<String> getDefinitionKeys() {
		Assertion.checkNotNull(definitionKeys);
		// ----------------------------------------------------------------------
		return definitionKeys;
	}
}
