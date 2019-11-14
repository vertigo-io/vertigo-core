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
package io.vertigo.core;

import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.daemon.DaemonManager;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.impl.analytics.AnalyticsManagerImpl;
import io.vertigo.core.impl.analytics.process.AnalyticsAspect;
import io.vertigo.core.impl.daemon.DaemonManagerImpl;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.analytics.log.SmartLoggerAnalyticsConnectorPlugin;
import io.vertigo.core.plugins.analytics.log.SocketLoggerAnalyticsConnectorPlugin;

/**
 * Defines core module.
 * @author pchretien
 */
public final class CoreFeatures extends Features<CoreFeatures> {

	/**
	 * Constructor.
	 */
	public CoreFeatures() {
		super("vertigo-core");
	}

	@Feature("analytics.socketLoggerConnector")
	public CoreFeatures withSocketLoggerAnalyticsConnector(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SocketLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	@Feature("analytics.smartLoggerConnector")
	public CoreFeatures withSmartLoggerAnalyticsConnector(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(SmartLoggerAnalyticsConnectorPlugin.class, params);
		return this;

	}

	/**
	 * Adds a AnalyticsConnectorPlugin
	 * @param analyticsConnectorPluginClass the plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CoreFeatures addAnalyticsConnectorPlugin(final Class<? extends AnalyticsConnectorPlugin> analyticsConnectorPluginClass, final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(analyticsConnectorPluginClass, params);
		return this;

	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class)
				.addAspect(AnalyticsAspect.class);
	}
}
