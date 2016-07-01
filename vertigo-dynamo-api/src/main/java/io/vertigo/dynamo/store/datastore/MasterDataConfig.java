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
package io.vertigo.dynamo.store.datastore;

import java.io.Serializable;

import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;

/**
 * Configuration des données de référence.
 * @author  pchretien
 */
public interface MasterDataConfig {
	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste est un filtrage simple sur la liste racine.
	 * @param uri URI
	 * @param fieldName Nom du champ de sélection
	 * @param value  Valeur de sélection
	 */
	void register(final DtListURIForMasterData uri, final String fieldName, final Serializable value);

	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste est un filtrage double sur la liste racine.
	 * @param uri URI
	 * @param fieldName1 Nom du premier champ de sélection
	 * @param value1  Valeur du premier champ de sélection
	 * @param fieldName2 Nom du second champ de sélection
	 * @param value2  Valeur du second champ de sélection
	 */
	void register(final DtListURIForMasterData uri, final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2);

	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste de référence est La liste racine.
	 * @param uri URI
	 */
	void register(final DtListURIForMasterData uri);

	/**
	 * Indique s'il existe une MasterDataList pour ce type d'objet.
	 * @param dtDefinition  Définition de DT
	 * @return True, s'il existe une MasterDataList
	 */
	boolean containsMasterData(final DtDefinition dtDefinition);

	/**
	 * Renvoi l'URI à partir d'une définition.
	 * @param dtDefinition DId de la Définition de DT
	 * @return URI de retour (notNUll)
	 */
	DtListURIForMasterData getDtListURIForMasterData(final DtDefinition dtDefinition);

	/**
	 * @param uri URI de la liste
	 * @return Fonction à appliquer sur la liste (par rapport à la liste complète).
	 */
	DtListProcessor getFilter(final DtListURIForMasterData uri);
}
