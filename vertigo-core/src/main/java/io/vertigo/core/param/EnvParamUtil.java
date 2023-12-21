/*
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
package io.vertigo.core.param;

import java.util.Optional;

import io.vertigo.core.lang.Assertion;

/**
 * Util to get params from Env or System.
 * Could be use, every where ParamManager wasn't load yet.
 *
 * @author npiedeloup
 */
public final class EnvParamUtil {

	private EnvParamUtil() {
		//empty for util class
	}

	public static Optional<Param> getParam(final String paramName, final String paramValue, final Optional<ParamManager> paramManagerOpt) {
		Assertion.check().isNotBlank(paramName);
		//-----
		if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}")) {
			final int defaultValueIdx = paramValue.indexOf("!");
			final String property = paramValue.substring("${".length(), defaultValueIdx > 0 ? defaultValueIdx : paramValue.length() - "}".length());
			final Optional<String> defaultValueOpt = defaultValueIdx > 0 ? Optional.of(paramValue.substring(defaultValueIdx + 1, paramValue.length() - "}".length())) : Optional.empty();
			if (paramManagerOpt.isPresent()) {
				return paramManagerOpt.get().getOptionalParam(property)
						.or(() -> Optional.of(Param.of(paramName, defaultValueOpt
								.orElseThrow(() -> new IllegalArgumentException("Param '" + property + "' not found (paramManager)")))));
			} else {
				return getOptionalSysEnvParam(paramName, property)
						.or(() -> Optional.of(Param.of(paramName, defaultValueOpt
								.orElseThrow(() -> new IllegalArgumentException("Param '" + property + "' not found (system and env)")))));
			}
		}
		return Optional.of(Param.of(paramName, paramValue));
	}

	private static Optional<Param> getOptionalSysEnvParam(final String paramName, final String property) {
		Assertion.check().isNotBlank(paramName);
		Assertion.check().isNotBlank(property);
		//-----
		String paramValue = System.getProperty(property);
		if (paramValue == null) {
			paramValue = System.getenv().get(property);
		}
		return paramValue != null ? Optional.of(Param.of(paramName, paramValue)) : Optional.empty();
	}
}
