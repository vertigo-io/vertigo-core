/**
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
package io.vertigo.core.node.component;

/**
 * Defines a contract for components that support activation and deactivation lifecycle behavior.
 * Implementations of this interface manage the lifecycle of components by providing start and stop operations.
 * - The start() method is invoked after the component is created and fully initialized (e.g., after dependency injection for components built via configuration).
 * - The stop()  method is invoked before the component is destroyed.
 *  
 * @author pchretien
 */
public interface Activeable {
	/**
	 * Initializes and activates the component.
	 * This method is called after the component has been created and fully initialized, typically after dependency injection for components built via configuration.
	 * Implementations should perform any necessary setup or resource allocation to prepare the component for use.
	 */
	void start();

	/**
	 * Deactivates and prepares the component for destruction.
	 * This method is called before the component is stopped or destroyed.
	 * Implementations should release resources, terminate processes, or perform cleanup to ensure a graceful shutdown.
	 */
	void stop();
}
