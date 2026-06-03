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
package io.vertigo.core.impl.locale;

import java.util.List;

import jakarta.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.ClassSelector;
import io.vertigo.core.lang.ClassSelector.ClassConditions;
import io.vertigo.core.locale.LocaleMessageKey;
import io.vertigo.core.node.definition.DefinitionSpace;
import io.vertigo.core.node.definition.SimpleDefinitionProvider;
import io.vertigo.core.param.ParamValue;

public final class LocaleDefinitionProvider implements SimpleDefinitionProvider {

	private final String packageName;

	@Inject
	public LocaleDefinitionProvider(
			@ParamValue("package") final String packageName) {
		Assertion.check().isNotBlank(packageName);
		//---
		this.packageName = packageName;
	}

	@Override
	public List<LocaleResourceDefinition> provideDefinitions(final DefinitionSpace definitionSpace) {
		Assertion.check().isNotNull(definitionSpace);
		//---
		return ClassSelector
				.from(packageName)
				.filterClasses(ClassConditions.subTypeOf(LocaleMessageKey.class))
				.filterClasses(ClassConditions.subTypeOf(Enum.class))
				.findClasses()
				.stream()
				.map(LocaleResourceDefinition::new)
				.toList();
	}

}
