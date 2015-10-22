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
package io.vertigo.core.spaces.resource;

import io.vertigo.core.impl.resource.ResourceResolverPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

/**
 * Selecteurs de ressources. 
 * Les ressources sont identifiées par une URL. 
 * Cette URL peut être 
 *  - relative au classpath de l'application dans le cas d'une application JAVA 
 *  - relative au context de l'application WEB 
 * 
 * La ressource peut aussi être résolue de façon ad-hoc par la création d'un plugin de résolution spécifique.
 * 
 * Les fichiers de configuration sont à considérer comme des ressources.  
 * Ex: 
 * 	classpath: 
 * 		/myproject/components/components-config.dtd
 * 	web:
 *     /WEB-INF/components-config.xml
 *  
 * L'implémentation permet de définir une liste de plusieurs plugins de résolutions de ressources.
 * Il est aussi possible d'enregistrer des @see ResourceResolverPlugin spécifique. (Par exemple pour stocker les ressources en BDD)
 *
 * @author pchretien
 */
public final class ResourceManager implements Component {
	private final List<ResourceResolverPlugin> resourceResolverPlugins;

	@Inject
	public ResourceManager(final List<ResourceResolverPlugin> resourceResolverPlugins) {
		Assertion.checkNotNull(resourceResolverPlugins);
		//-----
		this.resourceResolverPlugins = resourceResolverPlugins;
	}

	/**
	 * Retourne une URL à partir de sa représentation 'chaîne de caractères'
	 * 
	 * @param resource Url de la ressource(chaîne de caractères)
	 * @return URL associée à la ressource (Not Null)
	 */
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
