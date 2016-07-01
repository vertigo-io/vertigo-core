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
package io.vertigo.app.config;

import java.util.HashMap;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Plugin;

/**
 * Paramétrage d'un plugin :
 * - Type du plugin
 * - class du plugin
 * - paramètres du plugin
 *
 * Les paramètres sont :
 * - soit définies directement dans cette configuration,
 * - soit dans le fichier de conf lié à l'environnement.
 *
 * @author npiedeloup, pchretien
 */
public final class PluginConfig {
	private final String id;
	private final Class<? extends Plugin> implClass;
	private final Map<String, String> params;

	/**
	 * Constructeur.
	 * @param pluginImplClass Class du plugin
	 * @param params paramètres du plugin
	 */
	PluginConfig(final String id, final Class<? extends Plugin> pluginImplClass, final Map<String, String> params) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(pluginImplClass);
		Assertion.checkNotNull(params);
		//-----
		this.id = id;
		implClass = pluginImplClass;
		this.params = new HashMap<>(params);
	}

	/**
	 * @return Classe de l'implémentation du composant
	 */
	public Class<? extends Plugin> getImplClass() {
		return implClass;
	}

	/**
	 * @return Id 
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Map des paramètres du composant
	 */
	public Map<String, String> getParams() {
		return params;
	}

	@Override
	public String toString() {
		return id;
	}
}
