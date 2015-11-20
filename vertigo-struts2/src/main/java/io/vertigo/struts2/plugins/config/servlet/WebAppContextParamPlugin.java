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
package io.vertigo.struts2.plugins.config.servlet;

import io.vertigo.core.param.ParamPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.Map;

/**
 * Plugin d'accès à la configuration de la WebApp.
 * @author npiedeloup
*/
public final class WebAppContextParamPlugin implements ParamPlugin {
	private static Map<String, String> params;

	/**
	 * @param initConf Configuration initiale
	 */
	public static void setParams(final Map<String, String> params) {
		Assertion.checkNotNull(params);
		//-----
		WebAppContextParamPlugin.params = params;
	}

	/** {@inheritDoc} */
	@Override
	public Option<String> getValue(final String paramName) {
		Assertion.checkArgNotEmpty(paramName);
		//-----
		return params.containsKey(paramName) ? Option.<String> option(params.get(paramName)) : Option.<String> none();
	}
}
