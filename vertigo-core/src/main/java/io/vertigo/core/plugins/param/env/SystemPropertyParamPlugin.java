/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.plugins.param.env;

import java.util.Optional;

import io.vertigo.core.impl.param.ParamPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.param.Param;
import io.vertigo.core.util.StringUtil;

/**
 * Plugin de gestion de configuration des propriétés system.
 *
 * @author mlaroche
 */
public final class SystemPropertyParamPlugin implements ParamPlugin {
	/** {@inheritDoc} */
	@Override
	public Optional<Param> getParam(final String paramName) {
		Assertion.check().isNotBlank(paramName);
		//-----
		final String paramValue = System.getProperty(paramName);
		return StringUtil.isBlank(paramValue) ? Optional.empty() : Optional.of(Param.of(paramName, paramValue));
	}
}
