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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.di.DIAnnotationUtil;
import io.vertigo.core.util.StringUtil;

final class ConfigUtil {
	private ConfigUtil() {
		//
	}

	static List<ComponentConfig> buildConfigs(final List<PluginConfig> pluginConfigs) {
		Assertion.checkNotNull(pluginConfigs);
		//---
		final List<ComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();

		int index = 1;
		for (final PluginConfig pluginConfig : pluginConfigs) {
			final boolean added = pluginTypes.add(pluginConfig.getPluginType());
			final String id;
			if (added) {
				id = pluginConfig.getPluginType();
			} else {
				id = pluginConfig.getPluginType() + '#' + index;
				index++;
			}

			final ComponentConfig componentConfig = ComponentConfig.builder()
					.withImpl(pluginConfig.getImplClass())
					.withId(id)
					.addParams(pluginConfig.getParams())
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
			final String connectorType = StringUtil.first2LowerCase(DIAnnotationUtil.buildId(connectorConfig.getImplClass()));
			final boolean added = connectorTypes.add(connectorType);
			final String id;
			if (added) {
				id = connectorType;
			} else {
				id = connectorType + '#' + index;
				index++;
			}

			final ComponentConfig componentConfig = ComponentConfig.builder()
					.withImpl(connectorConfig.getImplClass())
					.withId(id)
					.addParams(connectorConfig.getParams())
					.build();
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

}
