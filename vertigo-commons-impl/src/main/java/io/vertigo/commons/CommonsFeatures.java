/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons;

import java.util.Optional;

import javax.inject.Named;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ComponentConfigBuilder;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.json.Feature;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.commons.impl.analytics.AnalyticsManagerImpl;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.commons.impl.daemon.DaemonManagerImpl;
import io.vertigo.commons.impl.eventbus.EventBusManagerImpl;
import io.vertigo.commons.impl.node.NodeInfosPlugin;
import io.vertigo.commons.impl.node.NodeManagerImpl;
import io.vertigo.commons.impl.node.NodeRegistryPlugin;
import io.vertigo.commons.impl.script.ScriptManagerImpl;
import io.vertigo.commons.impl.transaction.VTransactionAspect;
import io.vertigo.commons.impl.transaction.VTransactionManagerImpl;
import io.vertigo.commons.node.NodeManager;
import io.vertigo.commons.plugins.script.janino.JaninoExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.core.param.Param;

/**
 * Defines commons module.
 * @author pchretien
 */
public final class CommonsFeatures extends Features<CommonsFeatures> {

	/**
	 * Constructor.
	 */
	public CommonsFeatures() {
		super("commons");
	}

	/**
	 * Activates script with a default plugin.
	 *
	 * @return these features
	 */
	@Feature("script")
	public CommonsFeatures withScript() {
		getModuleConfigBuilder()
				.addComponent(ScriptManager.class, ScriptManagerImpl.class)
				.addPlugin(JaninoExpressionEvaluatorPlugin.class);
		return this;
	}

	/**
	 * Activates caches.
	 * @return these features
	 */
	@Feature("cache")
	public CommonsFeatures withCache() {
		getModuleConfigBuilder()
				.addComponent(CacheManager.class, CacheManagerImpl.class);
		return this;
	}

	public CommonsFeatures withCache(final Class<? extends CachePlugin> cachePluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addComponent(CacheManager.class, CacheManagerImpl.class)
				.addPlugin(cachePluginClass, params);
		return this;
	}

	/**
	 * Adds a REDIS connector.
	 * @param host the REDIS host
	 * @param port the REDIS port
	 * @param passwordOpt the REDIS password
	 * @param database the index of the REDIS database
	 * @return these features
	 */
	@Feature("redis")
	public CommonsFeatures withRedisConnector(final @Named("host") String host, final @Named("port") String port, final @Named("database") String database, @Named("password") final Optional<String> passwordOpt) {
		final ComponentConfigBuilder componentConfigBuilder = ComponentConfig
				.builder()
				.withImpl(RedisConnector.class)
				.addParam(Param.of("host", host))
				.addParam(Param.of("port", port))
				.addParam(Param.of("database", database));
		if (passwordOpt.isPresent()) {
			componentConfigBuilder
					.addParam(Param.of("password", passwordOpt.get()));
		}
		getModuleConfigBuilder()
				.addComponent(componentConfigBuilder.build());
		return this;

	}

	/**
	
	/**
	 * Adds a REDIS connector.
	 * @param host the REDIS host
	 * @param port the REDIS port
	 * @param passwordOpt the REDIS password
	 * @param database the index of the REDIS database
	 * @return these features
	 */
	public CommonsFeatures withRedisConnector(final String host, final int port, final int database, final Optional<String> passwordOpt) {
		return withRedisConnector(host, Integer.toString(port), Integer.toString(database), passwordOpt);

	}

	/**
	 * Adds a NodeRegistryPlugin
	 * @param nodeRegistryPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeRegistryPlugin(final Class<? extends NodeRegistryPlugin> nodeRegistryPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeRegistryPluginClass, params);
		return this;

	}

	/**
	 * Adds a NodeInfosPlugin
	 * @param nodeInfosPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withNodeInfosPlugin(final Class<? extends NodeInfosPlugin> nodeInfosPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(nodeInfosPluginClass, params);
		return this;

	}

	/**
	 * Adds a AnalyticsConnectorPlugin
	 * @param analyticsConnectorPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures addAnalyticsConnectorPlugin(final Class<? extends AnalyticsConnectorPlugin> analyticsConnectorPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(analyticsConnectorPluginClass, params);
		return this;

	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class)
				.addComponent(CodecManager.class, CodecManagerImpl.class)
				.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.addComponent(EventBusManager.class, EventBusManagerImpl.class)
				.addComponent(NodeManager.class, NodeManagerImpl.class)
				.addComponent(VTransactionManager.class, VTransactionManagerImpl.class)
				.addAspect(VTransactionAspect.class);
	}
}
