/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
import io.vertigo.core.node.Node;

/**
 * Utility class for retrieving parameters from the environment or system.
 * Can be used everywhere the ParamManager has not been loaded yet.
 *
 * @author: npiedeloup
 */
public final class ParamEnvUtil {

	/**
	 * Private constructor to prevent instantiation of the utility class.
	 */
	private ParamEnvUtil() {
		//Empty for utility class
	}

	/**
	 * Retrieves a parameter based on the specified name, value, and an optional ParamManager.
	 *
	 * @param paramName       The name of the parameter
	 * @param paramValue      The value of the parameter
	 * @param paramManagerOpt An optional ParamManager
	 * @return An Optional containing the retrieved Param, if available
	 * @throws IllegalArgumentException If the parameter is not found in either the	ParamManager or the system and environment
	 */
	public static Optional<Param> getParam(final String paramName, final String paramValue, final Optional<ParamManager> paramManagerOpt) {
		Assertion.check()
				.isNotBlank(paramName)
				.isNotNull(paramManagerOpt);
		// -----
		if (paramValue != null && paramValue.startsWith("${") && paramValue.endsWith("}")) {
			final int defaultValueIdx = paramValue.indexOf('!');
			final String property = paramValue.substring("${".length(), defaultValueIdx > 0 ? defaultValueIdx : paramValue.length() - "}".length());
			final Optional<String> defaultValueOpt = defaultValueIdx > 0
					? Optional.of(paramValue.substring(defaultValueIdx + 1, paramValue.length() - "}".length()))
					: Optional.empty();

			Optional<ParamManager> usedParamManagerOpt = paramManagerOpt;
			if (!paramManagerOpt.isPresent() && Node.getNode().getComponentSpace().contains("paramManager")) {
				usedParamManagerOpt = Optional.of(Node.getNode().getComponentSpace().resolve(ParamManager.class));
			}

			return (usedParamManagerOpt.isPresent()
					? usedParamManagerOpt.get().getOptionalParam(property)
					: getOptionalSysEnvParam(paramName, property))
							.or(() -> defaultValueOpt.map(defaultValue -> Param.of(paramName, defaultValue)));
		}
		return Optional.of(Param.of(paramName, paramValue));
	}

	/**
	 * Retrieves an optional system or environment parameter based on the specified parameter name.
	 *
	 * @param paramName The name of the parameter
	 * @param property  The property associated with the parameter
	 * @return An Optional containing the retrieved Param, if available
	 */
	private static Optional<Param> getOptionalSysEnvParam(final String paramName, final String property) {
		Assertion.check()
				.isNotBlank(paramName)
				.isNotBlank(property);
		// -----
		String paramValue = System.getProperty(property);
		if (paramValue == null) {
			paramValue = System.getenv().get(property);
		}
		return paramValue != null
				? Optional.of(Param.of(paramName, paramValue))
				: Optional.empty();
	}
}
