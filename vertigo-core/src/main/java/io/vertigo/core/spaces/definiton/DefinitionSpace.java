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
package io.vertigo.core.spaces.definiton;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.JsonExclude;

/**
 * Espace de définitions (non threadSafe).
 * Il est nécessaire d'enregistrer toutes les définitions au démarrage du serveur.
 * Etape 1 : Enregistrer les classes éligibles (register)
 * Etape 2 : Ajouter les objets (put)
 *
 * @author pchretien
 */
public final class DefinitionSpace implements Component, Activeable {
	@JsonExclude
	private final Map<String, Definition> allObjects = new LinkedHashMap<>(); //byId

	/**
	 * Enregistrement d'un nouvel object.
	 * @param definition Objet à enregistrer
	 */
	public void put(final Definition definition) {
		Assertion.checkNotNull(definition, "A definition can't be null.");
		//-----
		final String name = definition.getName();
		DefinitionUtil.checkName(name, definition.getClass());
		//-----
		final Definition previous2 = allObjects.put(name, definition);
		//On vérifie l'unicité globale du nom.
		Assertion.checkArgument(previous2 == null, "L'objet {0} est déja enregistré !", name);
	}

	/**
	 * @param name  Objet recherché
	 * @return Si un objet avec l'identifiant est déjà enregistré.
	 */
	public boolean contains(final String name) {
		return allObjects.containsKey(name);
	}

	/**
	 * Cette méthode ne doit être appelée que si l'objet est déjà enregistré.
	 * @param name Identifiant de l'objet
	 * @param clazz type de l'object
	 * @return Objet associé
	 * @param <D> Type de l'objet
	 */
	public <D extends Definition> D resolve(final String name, final Class<D> clazz) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(clazz);
		//-----
		final Definition definition = allObjects.get(name);
		Assertion.checkNotNull(definition, "Definition '{0}' of type '{1}' not found in ({2})", name, clazz.getSimpleName(), allObjects.keySet());
		return clazz.cast(definition);
	}

	/**
	 * @return Liste de tous les types de définition gérés.
	 */
	public Collection<Class<? extends Definition>> getAllTypes() {
		return allObjects.values()
				.stream()
				.map(Definition::getClass)
				.collect(Collectors.toSet());
	}

	/**
	 * @return Collection de tous les objets enregistrés pour un type donné.
	 * @param clazz type de l'object
	 * @param <C> Type de l'objet
	 */
	public <C extends Definition> Set<C> getAll(final Class<C> clazz) {
		Assertion.checkNotNull(clazz); // Le type des objets recherchés ne peut pas être null
		//-----
		return allObjects.values()
				.stream()
				.filter(definition -> clazz.isAssignableFrom(definition.getClass()))
				.map(clazz::cast)
				.collect(Collectors.toSet());
	}

	/**
	 * @return true if there is no definition in this space
	 */
	public boolean isEmpty() {
		return allObjects.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//nop
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		allObjects.clear();
	}

}
