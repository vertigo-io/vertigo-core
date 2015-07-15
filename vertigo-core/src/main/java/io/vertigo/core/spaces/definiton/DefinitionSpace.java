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
package io.vertigo.core.spaces.definiton;

import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Espace de définitions (non threadSafe).
 * Il est nécessaire d'enregistrer toutes les définitions au démarrage du serveur.
 * Etape 1 : Enregistrer les classes éligibles (register)
 * Etape 2 : Ajouter les objets (put)
 *
 * @author pchretien
 */
public final class DefinitionSpace implements Activeable {
	/**
	 * Liste des objets indexés par Class (le type) et nom.
	 */
	private final Map<Class<? extends Definition>, Map<String, Definition>> definitions = new HashMap<>();
	@JsonExclude
	private final Map<String, Definition> allObjects = new LinkedHashMap<>(); //byId

	/**
	 * Enregistrement d'une nouveau type d'objet géré par le space (éligibles).
	 * @param clazz Classe gérée
	 */
	private void register(final Class<? extends Definition> clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkArgument(!definitions.containsKey(clazz), "Type '{0}' deja enregistré", clazz.getName());
		//-----
		definitions.put(clazz, new LinkedHashMap<String, Definition>());
	}

	/**
	 * Enregistrement d'un nouvel object.
	 * @param definition Objet à enregistrer
	 */
	public void put(final Definition definition) {
		Assertion.checkNotNull(definition, "L'objet ne peut pas pas être null !");
		//-----
		if (!definitions.containsKey(definition.getClass())) {
			register(definition.getClass());
		}
		final Map<String, Definition> tobjects = definitions.get(definition.getClass());
		final String name = definition.getName();
		checkName(name, definition.getClass());
		final Definition previous = tobjects.put(name, definition);
		Assertion.checkArgument(previous == null, "L'objet {0} est déja enregistré !", name);
		//-----
		final Definition previous2 = allObjects.put(name, definition);
		//On vérifie l'unicité globale du nom.
		Assertion.checkState(previous2 == null, "L'objet {0} est déja enregistré !", name);
	}

	private static void checkName(final String name, final Class<? extends Definition> clazz) {
		final String prefix = DefinitionUtil.getPrefix(clazz);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgument(name.startsWith(prefix), "La définition {0} doit commencer par {1}", name, prefix);
		Assertion.checkArgument(name.length() > prefix.length(), "Le nom de la définition doit être renseigné");
		Assertion.checkArgument(name.toUpperCase().equals(name), "La définition {0} doit être en majuscules", name);
		Assertion.checkArgument(Definition.REGEX_DEFINITION_URN.matcher(name).matches(), "urn de définition {0} doit matcher le pattern {1}", name, Definition.REGEX_DEFINITION_URN);
	}

	/**
	 * @param definition  Definition recherché
	 * @return  Si une définition avec le nom précisé est déjà enregistrée.
	 */
	public boolean containsDefinition(final Definition definition) {
		return allObjects.containsValue(definition);
	}

	/**
	 * @param name  Objet recherché
	 * @return Si un objet avec l'identifiant est déjà enregistré.
	 */
	public boolean containsDefinitionName(final String name) {
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
		Assertion.checkArgument(definitions.containsKey(clazz), "Type de définition '{0}' non enregistré", clazz.getName());
		//-----
		final Map<String, Definition> tobjects = definitions.get(clazz);
		final Object o = tobjects.get(name);
		Assertion.checkNotNull(o, "Definition '{0}' non trouvé", name);
		return clazz.cast(o);
	}

	/**
	 * Récupération d'une définition par son nom.
	 * @param name Identifiant de l'objet
	 * @return Objet associé
	 */
	public Definition resolve(final String name) {
		final Definition object = allObjects.get(name);
		Assertion.checkNotNull(object, "Definition not found with name : {0}", name);
		return object;
	}

	/**
	 * @return Liste de tous les types de définition gérés.
	 */
	public Collection<Class<? extends Definition>> getAllTypes() {
		return Collections.unmodifiableCollection(definitions.keySet());
	}

	/**
	 * @return Collection de tous les objets enregistrés pour un type donné.
	 * @param clazz type de l'object
	 * @param <C> Type de l'objet
	 */
	public <C extends Definition> Collection<C> getAll(final Class<C> clazz) {
		Assertion.checkNotNull(clazz); // Le type des objets recherchés ne peut pas être null
		//-----
		if (definitions.containsKey(clazz)) {
			return (Collection<C>) definitions.get(clazz).values();
		}
		return Collections.emptyList();
	}

	public boolean isEmpty() {
		return definitions.isEmpty() && allObjects.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		//nop
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		definitions.clear();
		allObjects.clear();
	}

}
