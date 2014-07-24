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

import io.vertigo.kernel.component.Plugin;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Paramétrage de l'application.
 * 
 * @author npiedeloup, pchretien
 */
public final class PluginConfigBuilder implements Builder<PluginConfig> {
	private final Class<? extends Plugin> pluginImplClass;
	private final Map<String, String> params = new HashMap<>();
	private final Map<String, String> inheritedParams = new HashMap<>();
	private final ComponentConfigBuilder componentConfigBuilder;

	PluginConfigBuilder(final ComponentConfigBuilder componentConfigBuilder, final Class<? extends Plugin> pluginImplClass) {
		Assertion.checkNotNull(componentConfigBuilder);
		Assertion.checkNotNull(pluginImplClass);
		//---------------------------------------------------------------------
		this.pluginImplClass = pluginImplClass;
		this.componentConfigBuilder = componentConfigBuilder;
	}

	PluginConfigBuilder withInheritedParams(final Map<String, String> inheritedParams) {
		Assertion.checkNotNull(inheritedParams);
		//---------------------------------------------------------------------
		this.inheritedParams.putAll(inheritedParams);
		return this;
	}

	public PluginConfigBuilder withParam(final String paramName, final String paramValue) {
		Assertion.checkArgNotEmpty(paramName, "Parameter must not be empty");
		Assertion.checkNotNull(paramValue, "parameter '{0}' is required ", paramName);
		//---------------------------------------------------------------------
		params.put(paramName, paramValue);
		return this;
	}

	public ComponentConfigBuilder endPlugin() {
		return componentConfigBuilder;
	}

	/** {@inheritDoc} */
	public PluginConfig build() {
		return new PluginConfig(pluginImplClass, params);
	}
}
