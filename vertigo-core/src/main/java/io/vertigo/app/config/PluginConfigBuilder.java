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
package io.vertigo.app.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.component.di.DIAnnotationUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * The pluginConfigBuilder defines the configuration of a plugin.
 * A plugin is a way to parameterize a component.
  *
 * @author npiedeloup, pchretien
 * @param <B> the type of the parent
 */
public final class PluginConfigBuilder<B extends Builder> implements Builder<ComponentConfig> {
	private final Class<? extends Plugin> myPluginImplClass;
	private final Map<String, String> myParams = new HashMap<>();
	private final B myParentConfigBuilder;
	private final String pluginType;
	private Integer myIndex;

	/**
	 * Constructor.
	 * @param parentConfigBuilder the builder of the parent
	 * @param pluginImplClass impl of the plugin
	 */
	PluginConfigBuilder(final B parentConfigBuilder, final Class<? extends Plugin> pluginImplClass) {
		Assertion.checkNotNull(parentConfigBuilder);
		Assertion.checkNotNull(pluginImplClass);
		//-----
		myPluginImplClass = pluginImplClass;
		myParentConfigBuilder = parentConfigBuilder;
		pluginType = StringUtil.first2LowerCase(getType(pluginImplClass));
	}

	void withIndex(final int index) {
		myIndex = index;
	}

	String getPluginType() {
		return pluginType;
	}

	/*
	 * We are looking for the type of the plugin.
	 * This type is the first objector interface that inherits from then 'plugin' interface.
	 */
	private static String getType(final Class<? extends Plugin> pluginImplClass) {
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.

		for (final Class intf : ClassUtil.getAllInterfaces(pluginImplClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(intf);
			}
		}
		//We have found nothing among the interfaces.
		//we are drilling the classes to look for a class that inherits the plugin.
		for (Class currentClass = pluginImplClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
			if (Arrays.asList(currentClass.getInterfaces()).contains(Plugin.class)) {
				return DIAnnotationUtil.buildId(currentClass);
			}
		}
		throw new IllegalArgumentException("A plugin must extends an interface|class that defines its contract : " + pluginImplClass);
	}

	/**
	 * Adds a param to this plugin.
	 * @param paramName the name of the param
	 * @param paramValue the value of the param
	 * @return this builder
	 */
	public PluginConfigBuilder<B> addParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName, "Parameter must not be empty");
		//paramValue can be null
		//-----
		if (paramValue != null) {
			myParams.put(paramName, paramValue);
		}
		return this;
	}

	/**
	 * Ends this config of plugin.
	 * @return the builder of the module
	 */
	public B endPlugin() {
		return myParentConfigBuilder;
	}

	/** {@inheritDoc} */
	@Override
	public ComponentConfig build() {
		Assertion.checkNotNull(myIndex, "an index is required to define an id");
		//-----
		//By Convention only the second plugin of a defined type is tagged by its index #nn
		final String pluginId = myIndex == 0 ? pluginType : pluginType + "#" + myIndex;
		return new ComponentConfig(pluginId, Optional.empty(), myPluginImplClass, myParams);
	}
}
