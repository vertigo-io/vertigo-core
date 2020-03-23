/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.CoreComponent;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.component.di.DIAnnotationUtil;
import io.vertigo.core.util.ClassUtil;
import io.vertigo.core.util.StringUtil;

final class ConfigUtil {
	private ConfigUtil() {
		//
	}

	static List<ComponentConfig> buildPluginsComponentConfigs(final List<PluginConfig> pluginConfigs) {
		Assertion.checkNotNull(pluginConfigs);
		//---
		final List<ComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();

		int index = 1;
		for (final PluginConfig pluginConfig : pluginConfigs) {
			final String pluginType = StringUtil.first2LowerCase(getType(pluginConfig.getImplClass(), Plugin.class));
			final boolean added = pluginTypes.add(pluginType);
			final String id;
			if (added) {
				id = pluginType;
			} else {
				id = pluginType + '#' + index;
				index++;
			}

			final ComponentConfig componentConfig = ComponentConfig.builder()
					.withPlugin(pluginConfig.getImplClass(), pluginConfig.getParams(), id)
					.build();
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	static List<ComponentConfig> buildConnectorsComponentConfigs(final List<ConnectorConfig> connectorConfigs) {
		Assertion.checkNotNull(connectorConfigs);
		//---
		final List<ComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> connectorTypes = new HashSet<>();

		int index = 1;
		for (final ConnectorConfig connectorConfig : connectorConfigs) {
			final String connectorType = StringUtil.first2LowerCase(getType(connectorConfig.getImplClass(), Connector.class));
			final boolean added = connectorTypes.add(connectorType);
			final String id;
			if (added) {
				id = connectorType;
			} else {
				id = connectorType + '#' + index;
				index++;
			}

			final ComponentConfig componentConfig = ComponentConfig.builder()
					.withConnector(connectorConfig.getImplClass(), connectorConfig.getParams(), id)
					.build();
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	/*
	 * We are looking for the type of the plugin.
	 * This type is the first objector interface that inherits from then 'plugin' interface.
	 */
	private static String getType(final Class<? extends CoreComponent> implClass, final Class<? extends CoreComponent> componentType) {
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.

		for (final Class intf : ClassUtil.getAllInterfaces(implClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(componentType)) {
				return DIAnnotationUtil.buildId(intf);
			}
		}
		//We have found nothing among the interfaces.
		//we are drilling the classes to look for a class that inherits the plugin.
		for (Class currentClass = implClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
			if (Arrays.asList(currentClass.getInterfaces()).contains(componentType)) {
				return DIAnnotationUtil.buildId(currentClass);
			}
		}
		throw new IllegalArgumentException("A plugin must extends an interface|class that defines its contract : " + implClass);
	}

}
