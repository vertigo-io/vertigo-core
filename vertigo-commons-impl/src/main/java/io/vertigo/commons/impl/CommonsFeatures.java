/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.impl;

import io.vertigo.app.config.Features;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.eventbus.EventBusManager;
import io.vertigo.commons.impl.analytics.AnalyticsManagerImpl;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.daemon.DaemonManagerImpl;
import io.vertigo.commons.impl.eventbus.EventBusManagerImpl;
import io.vertigo.commons.impl.script.ExpressionEvaluatorPlugin;
import io.vertigo.commons.impl.script.ScriptManagerImpl;
import io.vertigo.commons.plugins.script.janino.JaninoExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.core.param.Param;

/**
 * Defines commons module.
 * @author pchretien
 */
public final class CommonsFeatures extends Features {

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
	public CommonsFeatures withScript() {
		getModuleConfigBuilder()
				.addComponent(ScriptManager.class, ScriptManagerImpl.class)
				.addPlugin(JaninoExpressionEvaluatorPlugin.class);
		return this;
	}

	/**
	 * Activates script with a defined plugin. 
	
	 * @param expressionEvaluatorPluginClass the type of plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withScript(Class<? extends ExpressionEvaluatorPlugin> expressionEvaluatorPluginClass, Param... params) {
		getModuleConfigBuilder()
				.addComponent(ScriptManager.class, ScriptManagerImpl.class)
				.addPlugin(expressionEvaluatorPluginClass, params);
		return this;
	}

	/**
	 * Activates caches.
	 *
	 * @param cachePluginClass the cache plugin to use
	 * @param params the params
	 * @return these features
	 */
	public CommonsFeatures withCache(final Class<? extends CachePlugin> cachePluginClass, Param... params) {
		getModuleConfigBuilder()
				.addComponent(CacheManager.class, CacheManagerImpl.class)
				.addPlugin(cachePluginClass, params);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(AnalyticsManager.class, AnalyticsManagerImpl.class)
				.addComponent(CodecManager.class, CodecManagerImpl.class)
				.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.addComponent(EventBusManager.class, EventBusManagerImpl.class);
	}
}
