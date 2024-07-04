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
package io.vertigo.core.impl.param;

import java.util.Optional;

import io.vertigo.core.node.component.Plugin;
import io.vertigo.core.param.Param;

/**
 * Interface for an application configuration management plugin.
 * All configurations are managed as Strings.
 * @author prahmoune
 */
public interface ParamPlugin extends Plugin {
    /**
     * Returns a configuration parameter.
     * Returns none if the parameter is not managed.
     * @param paramName Name of the parameter
     * @return Value of the parameter
     */
	Optional<Param> getParam(String paramName);
}
