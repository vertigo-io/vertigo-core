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
package io.vertigoimpl.commons.resource;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Option;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;


/**
 * Selecteur 
 *  - de classes
 *  - de ressources
 *  
 * L'impl�mentation permet de d�finir une liste de plusieurs plugins de r�solutions de ressources.
 * Il est aussi possible d'enregistrer des @see ResourceResolver sp�cifique. (Par exemple pour stocker les ressources en BDD)
 * L'enregistrement doit se faire lors de la phase de d�marrage.
 * 
 * @author pchretien
 * @version $Id: ResourceManagerImpl.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class ResourceManagerImpl implements ResourceManager {
	@Inject
	private List<ResourceResolverPlugin> resourceResolverPlugins;

	/** {@inheritDoc} */
	public URL resolve(final String resource) {
		for (final ResourceResolverPlugin resourceResolver : resourceResolverPlugins) {
			final Option<URL> url = resourceResolver.resolve(resource);
			if (url.isDefined()) {
				return url.get();
			}
		}
		//On n'a pas trouv� de resolver permettant de lire la ressource.
		throw new VRuntimeException("Ressource '{0}' non trouv�e", null, resource);
	}
}
