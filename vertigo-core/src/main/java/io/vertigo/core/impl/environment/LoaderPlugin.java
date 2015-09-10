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

import io.vertigo.core.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.lang.Plugin;

/**
 * Plugin de chargement de l'environnement.
 * @author pchretien
 */
public interface LoaderPlugin extends Plugin {
	/**
	 * Type parsed by loader.
	 * Examples : oom, kpr, eaxmi...
	 * @return Type parsed by loader
	 */
	String getType();

	/**
	 * Parsing des définitions pour un fichier (oom, kpr ou ksp)
	 * défini par une url (sur système de fichier ou classpath)
	 * et selon la grammaire en argument.
	 * @param resourcePath resourcePath
	 * @param dynamicModelRepository DynamicModelRepository
	 */
	void load(String resourcePath, DynamicDefinitionRepository dynamicModelRepository);

}
