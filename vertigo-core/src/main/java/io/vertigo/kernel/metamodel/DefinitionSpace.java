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
package io.vertigo.kernel.metamodel;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.JsonExclude;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Espace de définitions (non threadSafe).
 * Il est nécessaier d'enregistrer toutes les définitions au démarrage du serveur.
 * Etape 1 : Enregistrer les classes éligibles (register)
 * Etape 2 : Ajouter les objets (put) 
 * 
 * @author pchretien
 */
public final class DefinitionSpace implements Activeable {
	/**
	 * Liste des objets indexés par Class (le type) et identifiant.
	 */
	private final Map<Class<? extends Definition>, Map<String, Definition>> definitions = new HashMap<>();
	@JsonExclude
	private final Map<String, Definition> allObjects = new LinkedHashMap<>(); //byId

	/**
	 * Enregistrement d'une nouveau type d'objet géré par le space (éligibles).
	 * @param clazz Classe gérée
	 */
	public void register(final Class<? extends Definition> clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkArgument(!definitions.containsKey(clazz), "Type '{0}' deja enregistré", clazz.getName());
		//---------------------------------------------------------------------
		definitions.put(clazz, new LinkedHashMap<String, Definition>());
	}

	/**
	 * Enregistrement d'un nouvel object.
	 * @param definition Objet à enregistrer
	 * @param clazz type de l'object
	 */
	public void put(final Definition definition, final Class<? extends Definition> clazz) {
		Assertion.checkNotNull(definition, "L'objet ne peut pas pas être null !");
		Assertion.checkNotNull(clazz);
		Assertion.checkArgument(definitions.containsKey(clazz), "L'objet {0} ne peut pas pas être enregistré, son type  '{1}' est inconnu !", definition, clazz);
		// ----------------------------------------------------------------------
		final Map<String, Definition> tobjects = definitions.get(clazz);
		final String id = definition.getName();
		checkId(id, clazz);
		final Definition previous = tobjects.put(id, definition);
		Assertion.checkArgument(previous == null, "L'objet {0} est déja enregistré !", id);
		// ----------------------------------------------------------------------
		final Definition previous2 = allObjects.put(id, definition);
		//On vérifie l'unicité globale du nom.
		Assertion.checkState(previous2 == null, "L'objet {0} est déja enregistré !", id);
	}

	private void checkId(final String name, final Class<? extends Definition> clazz) {
		final String prefix = DefinitionUtil.getPrefix(clazz);
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgument(name.startsWith(prefix), "La définition {0} doit commencer par {1}", name, prefix);
		Assertion.checkArgument(name.length() > prefix.length(), "Le nom de la définition doit être renseigné");
		Assertion.checkArgument(name.toUpperCase().equals(name), "La définition {0} doit être en majuscules", name);
		Assertion.checkArgument(Definition.REGEX_DEFINITION_URN.matcher(name).matches(), "urn de définition {0} doit matcher le pattern {1}", name, Definition.REGEX_DEFINITION_URN);
	}

	/**
	 * @param value  Objet recherché 
	 * @return  Si un objet avec l'identifiant est déjà enregistré.
	 */
	public boolean containsValue(final Object value) {
		return allObjects.containsValue(value);
	}

	/**
	 * @param id  Objet recherché 
	 * @return Si un objet avec l'identifiant est déjà enregistré.
	 */
	public boolean containsKey(final String id) {
		return allObjects.containsKey(id);
	}

	/**
	 * Cette méthode ne doit être appelée que si l'objet est déjà enregistré.
	 * @param id Identifiant de l'objet
	 * @param clazz type de l'object
	 * @return Objet associé
	 * @param <C> Type de l'objet
	 */
	public <C extends Definition> C resolve(final String id, final Class<C> clazz) {
		Assertion.checkNotNull(id); // L'identifiant de l'objet recherché ne peut pas être null
		Assertion.checkNotNull(clazz);
		Assertion.checkArgument(definitions.containsKey(clazz), "Type '{0}' non enregistré", clazz.getName());
		//---------------------------------------------------------------------
		final Map<String, Definition> tobjects = definitions.get(clazz);
		final Object o = tobjects.get(id);
		Assertion.checkNotNull(o, "Object '{0}' non trouvé", id);
		return clazz.cast(o);
	}

	/**
	 * Récupération d'une définition par son URN.
	 * @param id Identifiant de l'objet
	 * @return Objet associé
	 */
	public Definition resolve(final String id) {
		final Definition object = allObjects.get(id);
		Assertion.checkNotNull(object, "Aucun Objet avec id = {0}", id);
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
		Assertion.checkArgument(definitions.containsKey(clazz), "Type '{0}' non enregistré", clazz.getName());
		//---------------------------------------------------------------------
		return (Collection<C>) definitions.get(clazz).values();
	}

	/** {@inheritDoc} */
	public void start() {
		Assertion.checkState(definitions.isEmpty(), "DefinitionSpace must be empty");
		Assertion.checkState(allObjects.isEmpty(), "DefinitionSpace must be empty");
	}

	/**
	 * Vide l'ensemble de ce container.
	 */
	/** {@inheritDoc} */
	public void stop() {
		definitions.clear();
		allObjects.clear();
	}
}
