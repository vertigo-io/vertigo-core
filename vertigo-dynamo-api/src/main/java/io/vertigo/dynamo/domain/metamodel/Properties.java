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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.JsonExclude;
import io.vertigo.core.lang.Modifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gestion de la flexibilité structurelle du modèle.
 * Permet d'ajouter des propriétés sur les concepts structurels.
 *
 * @author pchretien
 */
public final class Properties implements Modifiable {
	private final Map<Property<?>, Object> properties = new HashMap<>();
	@JsonExclude
	private boolean modifiable = true;

	/**
	 * Set des propriétés gérées.
	 * @return Collection
	 */
	public Set<Property<?>> getProperties() {
		return java.util.Collections.unmodifiableSet(properties.keySet());
	}

	/**
	 * Retourne la valeur d'une (méta) propriété liée au domaine, champ, dtDéfinition...
	 * null si cette propriété n'existe pas
	 * @param property Propriété
	 * @return valeur de la propriété
	 */
	public <T> T getValue(final Property<T> property) {
		Assertion.checkNotNull(property);
		//On ne vérifie rien sur le type retourné par le getter. 
		//le type a été validé lors du put.
		//----------------------------------------------------------------------
		//Conformémément au contrat, on retourne null si pas de propriété trouvée
		return property.getType().cast(properties.get(property));
	}

	/**
	 * Ajout d'une propriété typée.
	 * @param property propriété
	 * @param value Valeur de la propriété
	 */
	public <T> void putValue(final Property<T> property, final T value) {
		Assertion.checkNotNull(property);
		Assertion.checkArgument(modifiable, "Aucune propriété ne peut être ajoutée");
		Assertion.checkArgument(!properties.containsKey(property), "Propriété {0} déjà déclarée : ", property);
		//On vérifie que la valeur est du bon type
		property.getType().cast(value);
		//----------------------------------------------------------------------
		properties.put(property, value);
	}

	/** {@inheritDoc} */
	public void makeUnmodifiable() {
		modifiable = false;
	}

	/** {@inheritDoc} */
	public boolean isModifiable() {
		return modifiable;
	}
}
