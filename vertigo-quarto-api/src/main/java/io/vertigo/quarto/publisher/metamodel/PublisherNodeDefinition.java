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
package io.vertigo.quarto.publisher.metamodel;

import io.vertigo.lang.Assertion;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition d'un noeud dans une structure PublisherDataDefinition.
 * Un noeud contient des champs.
 * Les champs peuvent être :
 * - soit simples (valués) et de type Boolean, String ou Image
 * - soit un autre noeud
 * - soit une liste de noeuds
 *
 * @author npiedeloup, pchretien
 */
public final class PublisherNodeDefinition {
	private final Map<String, PublisherField> publisherFieldMap;

	PublisherNodeDefinition(final List<PublisherField> publisherFields) {
		Assertion.checkNotNull(publisherFields);
		//---------------------------------------------------------------------
		publisherFieldMap = new LinkedHashMap<>();
		for (final PublisherField publisherField : publisherFields) {
			registerField(publisherField);
		}
	}

	private void registerField(final PublisherField publisherField) {
		Assertion.checkArgument(!publisherFieldMap.containsKey(publisherField.getName()), "Le champ {0} est déjà déclaré.", publisherField.getName());
		//---------------------------------------------------------------------
		publisherFieldMap.put(publisherField.getName(), publisherField);
	}

	/**
	 * Retourne le champ correspondant SOUS CONDITION qu'il existe sinon assertion.
	 *
	 * @param fieldName Nom du champ
	 * @return Champ correspondant
	 */
	public PublisherField getField(final String fieldName) {
		final PublisherField field = publisherFieldMap.get(fieldName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(field, "Le champ {0} n''est pas dans la définition de ce noeud, champs disponibles [{1}]", fieldName, publisherFieldMap.keySet());
		return field;
	}

	/**
	 * @return Collection des champs.
	 */
	public Collection<PublisherField> getFields() {
		return publisherFieldMap.values();
	}
}
