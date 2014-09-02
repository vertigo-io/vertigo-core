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
package io.vertigo.kernel.di.configurator;

import io.vertigo.core.component.Plugin;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.JsonExclude;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.kernel.util.DIAnnotationUtil;
import io.vertigo.kernel.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
final class PluginConfig {
	private final Class<? extends Plugin> implClass;
	private final Map<String, String> params;
	@JsonExclude
	private final String pluginType;

	/**
	 * Constructeur.
	 * @param pluginImplClass Class du plugin
	 * @param params paramètres du plugin
	 */
	PluginConfig(final Class<? extends Plugin> pluginImplClass, final Map<String, String> params) {
		Assertion.checkNotNull(pluginImplClass);
		Assertion.checkNotNull(params);
		//---------------------------------------------------------------------
		this.pluginType = StringUtil.normalize(getType(pluginImplClass));
		implClass = pluginImplClass;
		this.params = new HashMap<>(params);
	}

	/*
	 * On cherche le type du plugin qui correspond à la première interface ou classe qui hérite de Plugin.
	 */
	private static String getType(Class<? extends Plugin> pluginImplClass) {
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.

		for (Class intf : ClassUtil.getAllInterfaces(pluginImplClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(intf);
			}
		}
		//On n'a pas trouvé dans les interfaces on attaque les classes en cherchant une classe qui implémente Plugin
		for (Class currentClass = pluginImplClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
			if (Arrays.asList(currentClass.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(currentClass);
			}
		}
		throw new IllegalArgumentException("A plugin must extends an interface|class that defines its contract : " + pluginImplClass);
	}

	/**
	 * @return Classe de l'implémentation du composant
	 */
	Class<? extends Plugin> getImplClass() {
		return implClass;
	}

	/**
	 * @return Type du plugin
	 */
	String getType() {
		return pluginType;
	}

	/**
	 * @return Map des paramètres du composant
	 */
	Map<String, String> getParams() {
		return params;
	}
}
