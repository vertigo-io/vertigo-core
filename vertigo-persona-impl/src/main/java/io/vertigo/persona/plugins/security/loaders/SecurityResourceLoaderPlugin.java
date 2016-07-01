/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.persona.plugins.security.loaders;

import javax.inject.Inject;

import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.definition.loader.LoaderPlugin;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class SecurityResourceLoaderPlugin implements LoaderPlugin {
	private final ResourceManager resourceManager;

	/**
	 * Constructor.
	 * @param resourceManager the resourceManager
	 */
	@Inject
	public SecurityResourceLoaderPlugin(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		//-----
		this.resourceManager = resourceManager;

	}

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "security";
	}

	/** {@inheritDoc} */
	@Override
	public void load(final String resourcePath, final DynamicDefinitionRepository dynamicModelRepository) {
		final XmlSecurityLoader xmlSecurityLoader = new XmlSecurityLoader(resourceManager, resourcePath);
		xmlSecurityLoader.load();
	}
}
