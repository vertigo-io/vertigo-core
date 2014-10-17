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
package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration logique des stores physiques.
 * @author pchretien, npiedeloup
 */
abstract class AbstractLogicalStoreConfiguration<D extends Definition, S> {
	/** Store physique par défaut. */
	private S defaultStore;

	/** Map des stores utilisés spécifiquement pour certains DT */
	private final Map<D, S> storeMap = new HashMap<>();

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param definition Définition 
	 * @return Store utilisé pour cette definition
	 */
	protected final S getPhysicalStore(final D definition) {
		Assertion.checkNotNull(definition);
		//---------------------------------------------------------------------
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		S physicalStore = storeMap.get(definition);

		physicalStore = physicalStore == null ? defaultStore : physicalStore;
		Assertion.checkNotNull(physicalStore, "Aucun store trouvé pour la définition '{0}'", definition.getName());
		return physicalStore;
	}

	/**
	 * Enregistre un Store spécifique pour une dtDefinition donnée.
	 * @param definition Définition
	 * @param specificStore Store spécifique
	 */
	public final void register(final D definition, final S specificStore) {
		//check();
		Assertion.checkNotNull(definition);
		Assertion.checkNotNull(specificStore);
		Assertion.checkArgument(!storeMap.containsKey(definition), "Un store spécifique est déjà enregistré pour cette definition ''{0}'')", storeMap.get(definition));
		//----------------------------------------------------------------------
		storeMap.put(definition, specificStore);
	}

	public final void registerDefaultPhysicalStore(final S tmpDefaultStore) {
		Assertion.checkNotNull(tmpDefaultStore);
		Assertion.checkState(defaultStore == null, "defaultStore deja initialisé");
		//---------------------------------------------------------------------
		defaultStore = tmpDefaultStore;
	}
}
