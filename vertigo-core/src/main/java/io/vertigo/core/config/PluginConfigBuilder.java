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
package io.vertigo.core.config;

import io.vertigo.core.di.DIAnnotationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Paramétrage de l'application.
 *
 * @author npiedeloup, pchretien
 */
public final class PluginConfigBuilder implements Builder<PluginConfig> {
	private final Class<? extends Plugin> myPluginImplClass;
	private final Map<String, String> myParams = new HashMap<>();
	private final ComponentConfigBuilder myComponentConfigBuilder;
	private final String pluginType;
	private Integer myIndex;

	PluginConfigBuilder(final ComponentConfigBuilder componentConfigBuilder, final Class<? extends Plugin> pluginImplClass) {
		Assertion.checkNotNull(componentConfigBuilder);
		Assertion.checkNotNull(pluginImplClass);
		//-----
		this.myPluginImplClass = pluginImplClass;
		this.myComponentConfigBuilder = componentConfigBuilder;
		this.pluginType = StringUtil.first2LowerCase(getType(pluginImplClass));
	}

	void withIndex(final int index) {
		myIndex = index;
	}

	public String getPluginType() {
		return pluginType;
	}

	/*
	 * On cherche le type du plugin qui correspond à la première interface ou classe qui hérite de Plugin.
	 */
	private static String getType(final Class<? extends Plugin> pluginImplClass) {
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.

		for (final Class intf : ClassUtil.getAllInterfaces(pluginImplClass)) {
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

	public PluginConfigBuilder addParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName, "Parameter must not be empty");
		Assertion.checkNotNull(paramValue, "parameter '{0}' is required ", paramName);
		//-----
		myParams.put(paramName, paramValue);
		return this;
	}

	public ComponentConfigBuilder endPlugin() {
		return myComponentConfigBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public PluginConfig build() {
		Assertion.checkNotNull(myIndex, "an index is required to define an id");
		//-----
		//By Convention only the second plugin of a defined type is tagged by its index #nn
		final String pluginId = (myIndex == 0) ? pluginType : pluginType + "#" + myIndex;
		return new PluginConfig(pluginId, myPluginImplClass, myParams);
	}
}
