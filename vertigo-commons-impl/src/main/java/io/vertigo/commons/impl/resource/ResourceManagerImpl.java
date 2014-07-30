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
package io.vertigo.commons.impl.resource;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

/**
 * Selecteur 
 *  - de classes
 *  - de ressources
 *  
 * L'implémentation permet de définir une liste de plusieurs plugins de résolutions de ressources.
 * Il est aussi possible d'enregistrer des @see ResourceResolver spécifique. (Par exemple pour stocker les ressources en BDD)
 * L'enregistrement doit se faire lors de la phase de démarrage.
 * 
 * @author pchretien
 */
public final class ResourceManagerImpl implements ResourceManager {
	private final List<ResourceResolverPlugin> resourceResolverPlugins;

	@Inject
	public ResourceManagerImpl(final List<ResourceResolverPlugin> resourceResolverPlugins) {
		Assertion.checkNotNull(resourceResolverPlugins);
		//---------------------------------------------------------------------
		this.resourceResolverPlugins = resourceResolverPlugins;
	}

	/** {@inheritDoc} */
	public URL resolve(final String resource) {
		for (final ResourceResolverPlugin resourceResolver : resourceResolverPlugins) {
			final Option<URL> url = resourceResolver.resolve(resource);
			if (url.isDefined()) {
				return url.get();
			}
		}
		//On n'a pas trouvé de resolver permettant de lire la ressource.
		throw new RuntimeException("Ressource '" + resource + "' non trouvée");
	}
}
