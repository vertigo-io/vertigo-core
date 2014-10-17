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
package io.vertigo.commons.resource;

import io.vertigo.core.spaces.component.Manager;

import java.net.URL;

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
 * @author pchretien
 */
public interface ResourceManager extends Manager {
	/**
	 * Retourne une URL à partir de sa représentation 'chaîne de caractères'
	 * 
	 * @param resource Url de la ressource(chaîne de caractères)
	 * @return URL associée à la ressource (Not Null)
	 */
	URL resolve(String resource);
}
