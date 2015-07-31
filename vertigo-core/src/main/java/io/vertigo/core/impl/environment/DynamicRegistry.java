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
package io.vertigo.core.impl.environment;

import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.lang.Option;

import java.util.List;

/**
 * Handler qui permet de créer des définitions statiques à partir d'une définition dynamique.
 * @author pchretien
 */
public interface DynamicRegistry {
	/**
	 * @return Grammaire
	 */
	Grammar getGrammar();

	/**
	 * 
	 * @return Liste des definitions de base (String ....) permettant de construire les autes
	 */
	List<DynamicDefinition> getRootDynamicDefinitions();

	/**
	 * Enregistrement d'une définition.
	 * @param definition Définition
	 */
	Option<Definition> createDefinition(DynamicDefinition definition);

	/**
	 * Ajout d'une définition.
	 * Utilisé pour créer des définitions à partir d'autres Definitions.
	 * Exemple : création des domaines à partir d'un DT.
	 * 
	 * @param xdefinition DynamicDefinition
	 * @param dynamicModelrepository DynamicModelRepository
	 */
	void onNewDefinition(final DynamicDefinition xdefinition, final DynamicDefinitionRepository dynamicModelrepository);
}
