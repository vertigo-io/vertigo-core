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
package io.vertigo.core.impl.environment.kernel.model;

import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityProperty;

import java.util.List;
import java.util.Set;

/**
 * Classe permettant de créer dynamiquement une structure grammaticale.
 * Cette Classe est utilisée pour parcourir dynamiquement les modèles.
 * Rappelons qu'une structure est elle-même composée de sous structure grammaticales.
 *
 * @author  pchretien
 */
public interface DynamicDefinition {
	/**
	 * @return Clé de la Définition
	 */
	DynamicDefinitionKey getDefinitionKey();

	/**
	 * @return Nom du package
	 */
	String getPackageName();

	/**
	 * @return Entité
	 */
	Entity getEntity();

	/**
	 * Retourne la valeur d'une (méta) propriété liée au domaine, champ, dtDéfinition...
	 * null si cette propriété n'existe pas
	 * @param property Propriété
	 * @return valeur de la propriété
	 */
	Object getPropertyValue(EntityProperty property);

	/**
	 * Set des propriétés gérées.
	 * @return Collection
	 */
	Set<EntityProperty> getProperties();

	/**
	 * Permet de récupérer la liste des définitions d'un champ.
	 *
	 * @param fieldName Nom du champ.
	 * @return List
	 */
	List<DynamicDefinitionKey> getDefinitionKeys(final String fieldName);

	/**
	 * Uniquement si il y a une et une seule référence pour ce champ.
	 * @param fieldName Nom du champ.
	 * @return Clé de la définition
	 */
	DynamicDefinitionKey getDefinitionKey(final String fieldName);

	/**
	 * @param fieldName Nom du champ.
	 * @return Si la définition contient le champ
	 */
	boolean containsDefinitionKey(final String fieldName);

	/**
	 * Permet de récupérer la collection de toutes les liste de définitions utilisées par référence.
	 * @return Collection de toutes les liste de définitions référencées.
	 */
	List<DynamicDefinitionKey> getAllDefinitionKeys();

	/**
	 * Récupération de la liste des definitions dont est composée la définition principale.
	 * @param fieldName String
	 * @return List
	 */
	List<DynamicDefinition> getChildDefinitions(final String fieldName);

	/**
	 * @return Collection des listes de définitions composites.
	 */
	List<DynamicDefinition> getAllChildDefinitions();

	//-----
	/**
	 * Controle et valide les données de cette définition.
	 */
	void check();
}
