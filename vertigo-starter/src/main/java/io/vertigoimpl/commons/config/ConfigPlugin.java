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
package io.vertigoimpl.commons.config;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.kernel.lang.Option;

/**
 * Interface d'un plugin de gestion de configuration applicative. 
 * Toutes les configuration sont g�r�es sous forme de String.
 * @author prahmoune
 * @version $Id: ConfigPlugin.java,v 1.1 2013/10/09 14:02:59 pchretien Exp $ 
 */
public interface ConfigPlugin extends Plugin {
	/** 
	 * Retourne une propri�t� de configuration.
	 * Retourne none si la propri�t� n'est pas g�r�e.
	 * @param config Nom de la configuration
	 * @param propertyName Nom de la propri�t� de la configuration
	 * @return Valeur de la propri�t�
	 */
	Option<String> getValue(String config, String propertyName);
}
