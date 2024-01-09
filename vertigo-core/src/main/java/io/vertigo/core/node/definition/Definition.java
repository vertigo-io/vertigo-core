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
package io.vertigo.core.node.definition;

/**
 * The Definition interface defines an element that represents a part of the business or technical model.
 * Each definition has a unique identifier, has a unique name which is composed of a specific prefix.
 * Definitions are immutable, not serializable, and are loaded at boot time.
 *
 * @author pchretien
 */
public interface Definition {
	/**
	 * Gets the identifier of the definition.
	 *
	 * @return the unique identifier of the definition
	 */
	DefinitionId id();

	/**
	 * Gets the name of the definition.
	 *
	 * @return the name of the definition
	 */
	default String getName() {
		return id().fullName();
	}
}
