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
package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * @author pchretien
 */
public final class KernelGrammar extends GrammarProvider {
	public static final KernelGrammar INSTANCE = new KernelGrammar();

	/** Mot-clé des MetaDefinitions de DataType. */
	private static final String DATA_TYPE_META_DEFINITION = "DataType";

	/**Type Primitif.*/
	private final Entity dataTypeEntiy;

	/**Définition d'un champ de DT.*/

	/**
	 * Initialisation des métadonnées permettant de décrire le métamodèle .
	 */
	private KernelGrammar() {
		dataTypeEntiy = new EntityBuilder(DATA_TYPE_META_DEFINITION).build();
		//---------------------------------------------------------------------
		getGrammar().registerEntity(dataTypeEntiy);
	}

	/**
	 * @return Type primitif.
	 */
	public Entity getDataTypeEntity() {
		return dataTypeEntiy;
	}
}
