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

import java.util.List;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * This class defines the configuration of a plugin.
 *
 * A plugin is defined by
 *  - a implemenation class
 *  - a map of params
 *  - a type
 *
 * Several plugins may have the same type.
 *  - for example : a metaDataExtractor plugin has many implementations to deal with all the formats
 * The same plugin can be used many times with distincts params
 *  - for example : a plugin to listen on a specific channel will be used with many params (as many as channels)
 *
 * @author npiedeloup, pchretien
 */
public final class PluginConfig {
	private final Class<? extends Plugin> implClass;
	private final List<Param> params;

	/**
	 * Constructor.
	 * @param implClass the impl class of the component
	 * @param params the params
	 */
	PluginConfig(final Class<? extends Plugin> implClass, final List<Param> params) {
		Assertion.checkNotNull(implClass);
		Assertion.checkArgument(Plugin.class.isAssignableFrom(implClass), "impl class {0} must implement {1}", implClass, Plugin.class);
		Assertion.checkNotNull(params);
		//-----
		this.implClass = implClass;
		this.params = params;
	}

	/**
	 * @return the impl class of the component
	 */
	public Class<? extends Plugin> getImplClass() {
		return implClass;
	}

	/**
	 * @return the params
	 */
	public List<Param> getParams() {
		return params;
	}
}
