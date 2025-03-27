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

import io.vertigo.core.node.component.Manager;

/**
 * Manager interface for application configuration.
 *
 * The configuration consists of a list of parameters, each identified by a name.
 * Parameter names are camelCase and contain only letters and digits, with dots as separators.
 *
 * The parameters can be of three types:
 * - boolean
 * - String
 * - int
 *
 * Example in JSON:
 *
 * {
 *  "server.host" : "wiki",
 *  "server.port" : "5455",
 *  "maxUsers"  :"10"
 * }
 *
 * Example usage:
 *
 * getStringValue("server.host") // Returns "wiki"
 * getStringValue("host") // Throws an error
 *
 * @author: pchretien, npiedeloup, prahmoune
 *
 * @see Param
 */
public interface ParamManager extends Manager {
	/**
     * Returns the value for a parameter identified by its name.
     *
     * @param paramName the name of the parameter
     * @return the value of the parameter
     */
	Param getParam(String paramName);

	/**
     * Returns the optional value for a parameter, which may be present or not, identified by its name.
     *
     * @param paramName the name of the parameter
     * @return the optional value of the parameter
     */
	Optional<Param> getOptionalParam(String paramName);
}
