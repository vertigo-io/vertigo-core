/**
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
package io.vertigo.core.node.definition;

import java.util.Arrays;
import java.util.List;

import io.vertigo.core.lang.Assertion;

/**
 * Provides a list of definitions through an enum.
 *
 * @author skerdudou
 */
public interface SimpleEnumDefinitionProvider<D extends Definition> extends DefinitionProvider {

	/**
	 * Return a list of definitions with a set of already known definitions
	 *
	 * @param definitionSpace the actual definitionSpace
	 * @return the list of new definition to register
	 */
	@Override
	default List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		Assertion.check().isNotNull(definitionSpace);
		//---
		final var enumValues = getEnumClass().getEnumConstants();

		return Arrays.stream(enumValues)
				.map(v -> (DefinitionSupplier) v::buildDefinition)
				.toList();
	}

	Class<? extends EnumDefinition<D, ? extends Enum<?>>> getEnumClass();

	static interface EnumDefinition<D, T extends Enum<T>> {

		String getDefinitionName();

		D buildDefinition(DefinitionSpace definitionSpace);

		D get();

	}
}
