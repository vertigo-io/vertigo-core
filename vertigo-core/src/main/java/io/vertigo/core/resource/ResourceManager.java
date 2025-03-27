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
package io.vertigo.core.resource;

import java.net.URL;

import io.vertigo.core.node.component.Manager;

/**
 * Resource Managers for resource selection.
 * Resources are identified by a URL.
 * This URL can be
 *  - relative to the application's classpath in the case of a Java application
 *  - relative to the context of the WEB application
 *
 * The resource can also be resolved ad-hoc by creating a specific resolution plugin.
 *
 * Configuration files are to be considered as resources.
 * Example:
 *   classpath:
 *       /myproject/components/components-config.dtd
 *   web:
 *       /WEB-INF/components-config.xml
 *
 * The implementation allows defining a list of several resource resolution plugins.
 * It is also possible to register specific @see ResourceResolverPlugin. (For example, to store resources in a database)
 *
 * @author: pchretien
 */
public interface ResourceManager extends Manager {
    /**
     * Returns a URL from its 'string' representation.
     *
     * @param resource URL of the resource (string representation)
     * @return URL associated with the resource (NotNull)
     */
	URL resolve(final String resource);

}
