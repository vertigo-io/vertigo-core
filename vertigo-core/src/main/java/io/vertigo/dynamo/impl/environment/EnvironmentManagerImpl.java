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

import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.lang.Assertion;

import java.util.List;

import javax.inject.Inject;

/**
 * Manager de chargement de l'environnement. Ce manager ce paramètre par l'ajout
 * de plugins implémentant DynamicHandler. Chaque plugins permet d'enrichir la
 * grammaire et de transposer les DynamicDefinition lues dans les NameSpaces des
 * Managers idoines.
 *
 * @author pchretien, npiedeloup
 */
public final class EnvironmentManagerImpl implements EnvironmentManager {
	private final List<LoaderPlugin> loaderPlugins;
	private final List<DynamicRegistryPlugin> dynamicRegistryPlugins;

	@Inject
	public EnvironmentManagerImpl(final List<LoaderPlugin> loaderPlugins, final List<DynamicRegistryPlugin> dynamicRegistryPlugins) {
		Assertion.checkNotNull(loaderPlugins);
		Assertion.checkNotNull(dynamicRegistryPlugins);
		//-----
		this.dynamicRegistryPlugins = dynamicRegistryPlugins;
		this.loaderPlugins = loaderPlugins;
	}

	@Override
	public DefinitionLoader createDefinitionLoader() {
		return new DefinitionLoader(dynamicRegistryPlugins, loaderPlugins);
	}
}
