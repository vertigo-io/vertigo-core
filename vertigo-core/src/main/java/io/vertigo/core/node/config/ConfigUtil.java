/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.node.component.di.DIAnnotationUtil;
import io.vertigo.core.util.ClassUtil;

final class ConfigUtil {
	private static final Logger LOG = LogManager.getLogger(ConfigUtil.class);
	/** Beyond this suffix, injected lists no longer follow the declaration order (see buildIndexedId). */
	private static final int MAX_ORDERED_INDEX = 9;
	/** Ids already reported : component configs are rebuilt many times at startup (HealthUtil, MetricUtil) */
	private static final Set<String> WARNED_IDS = ConcurrentHashMap.newKeySet();

	private ConfigUtil() {
		//
	}

	/*
	 * Duplicated types get the suffix '#index', with one counter shared by all the types of the list.
	 * DIInjector injects a list sorted alphabetically by id : beyond #9, '#10' sorts before '#2',
	 * so the injected order no longer follows the declaration order (ex : ParamPlugins, the first one that finds a param wins).
	 * Latent defect, not fixed yet : the warning tells whether a real configuration reaches it.
	 */
	private static String buildIndexedId(final String type, final int index) {
		if (index > MAX_ORDERED_INDEX && WARNED_IDS.add(type + '#' + index)) {
			LOG.warn("Component id '{}#{}' : beyond #{} the injected lists of this type may not follow the declaration order (ids are sorted alphabetically, '#10' before '#2')",
					type, index, MAX_ORDERED_INDEX);
		}
		return type + '#' + index;
	}

	static List<CoreComponentConfig> buildPluginsComponentConfigs(final List<PluginConfig> pluginConfigs) {
		Assertion.check()
				.isNotNull(pluginConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> pluginTypes = new HashSet<>();

		int index = 1;
		for (final PluginConfig pluginConfig : pluginConfigs) {
			final String pluginType = DIAnnotationUtil.buildId(pluginConfig.apiClass());
			final boolean added = pluginTypes.add(pluginType);
			final String id;
			if (added) {
				id = pluginType;
			} else {
				id = buildIndexedId(pluginType, index);
				index++;
			}

			final CoreComponentConfig componentConfig = CoreComponentConfig.createPlugin(id, pluginConfig.implClass(), pluginConfig.params());
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	static List<CoreComponentConfig> buildConnectorsComponentConfigs(final List<ConnectorConfig> connectorConfigs) {
		Assertion.check()
				.isNotNull(connectorConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		final Set<String> connectorTypes = new HashSet<>();

		int index = 1;
		for (final ConnectorConfig connectorConfig : connectorConfigs) {
			final String connectorType = DIAnnotationUtil.buildId(
					connectorConfig.apiClassOpt()
							.orElseGet(connectorConfig::implClass));

			final boolean added = connectorTypes.add(connectorType);
			final String id;
			if (added) {
				id = connectorType;
			} else {
				id = buildIndexedId(connectorType, index);
				index++;
			}

			final CoreComponentConfig componentConfig = CoreComponentConfig.createConnector(id, connectorConfig.implClass(), connectorConfig.params());
			componentConfigs.add(componentConfig);
		}
		return componentConfigs;
	}

	static List<CoreComponentConfig> buildComponentConfigs(final List<ComponentConfig> componentConfigs) {
		Assertion.check()
				.isNotNull(componentConfigs);
		//---
		final List<CoreComponentConfig> coreComponentConfigs = new ArrayList<>();
		for (final ComponentConfig componentConfig : componentConfigs) {
			//By convention the component id is the simpleName of the api or the impl
			final String id = DIAnnotationUtil.buildId(
					componentConfig.apiClassOpt()
							.orElseGet(componentConfig::implClass));

			final CoreComponentConfig coreComponentConfig = CoreComponentConfig.createComponent(id,
					componentConfig.apiClassOpt(),
					componentConfig.implClass(),
					componentConfig.params());
			coreComponentConfigs.add(coreComponentConfig);
		}
		return coreComponentConfigs;
	}

	private static <C> Optional<Class<? extends C>> getApiOpt(final Class<? extends C> implClass, final Class<C> superApi) {
		Assertion.check()
				.isNotNull(implClass);
		//---
		//We are seeking the first and unique Object that extends Plugin.
		//This Interface defines the type of the plugin.
		for (final Class intf : ClassUtil.getAllInterfaces(implClass)) {
			if (Arrays.asList(intf.getInterfaces()).contains(superApi)) {
				return Optional.of(intf);
			}
		}
		return Optional.empty();
	}

	static Optional<Class<? extends Connector>> getConnectorApiOpt(final Class<? extends Connector> implClass) {
		return getApiOpt(implClass, Connector.class);
	}

	/*
	 * We are looking for the type of the plugin.
	 * This type is the first objector interface that inherits from then 'plugin' interface.
	 */
	static Class<? extends Plugin> getPluginApi(final Class<? extends Plugin> implClass) {
		return getApiOpt(implClass, Plugin.class)
				.orElseThrow(() -> new IllegalArgumentException("A plugin impl must extend an interface that defines its api: " + implClass));
	}

	static List<CoreComponentConfig> buildAmplifiersComponentConfigs(final List<AmplifierConfig> amplifierConfigs) {
		Assertion.check()
				.isNotNull(amplifierConfigs);
		//---
		final List<CoreComponentConfig> componentConfigs = new ArrayList<>();
		for (final AmplifierConfig amplifierConfig : amplifierConfigs) {
			final String id = DIAnnotationUtil.buildId(amplifierConfig.apiClass());
			final CoreComponentConfig coreComponentConfig = CoreComponentConfig.createAmplifier(id, amplifierConfig.apiClass(), amplifierConfig.params());
			componentConfigs.add(coreComponentConfig);
		}
		return componentConfigs;
	}
}
