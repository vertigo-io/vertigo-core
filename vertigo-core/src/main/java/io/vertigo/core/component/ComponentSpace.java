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
package io.vertigo.core.component;

import io.vertigo.core.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.lang.Activeable;

/**
 * Centralisation des accès aux composants et aux plugins d'un module.
 * Les composants sont d'un type M.
 * @author pchretien
 */
public interface ComponentSpace extends Container, Activeable {
	/**
	 * @param componentClass Classe type du composant(Interface)
	 * @param <T> Type du composant
	 * @return Gestionnaire centralisé des documents.
	 */
	<T> T resolve(final Class<T> componentClass);

	ComponentSpaceConfig getConfig();
}
