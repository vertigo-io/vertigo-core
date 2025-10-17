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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.locale.LocaleMessageKey;
import io.vertigo.core.node.definition.AbstractDefinition;
import io.vertigo.core.node.definition.DefinitionPrefix;

/**
 * Definition for a locale resource (enum or explicit array of LocaleMessageKey).
 */
@DefinitionPrefix(LocaleResourceDefinition.PREFIX)
public final class LocaleResourceDefinition extends AbstractDefinition<LocaleResourceDefinition> {
	public static final String PREFIX = "LocalResource";

	private final String baseName;
	private final LocaleMessageKey[] keys;

	private static String buildName(final Class<?> resourceClass) {
		return PREFIX + resourceClass.getSimpleName()
				+ "$h" + Integer.toHexString(resourceClass.hashCode());
	}

	/**
	 * Constructor for enum-based resource. baseName is deduced from class canonical name.
	 */
	public <E extends Enum<E> & LocaleMessageKey> LocaleResourceDefinition(final Class<E> resourceClass) {
		super(buildName(resourceClass));
		Assertion.check()
				.isNotNull(resourceClass);
		baseName = resourceClass.getCanonicalName();
		keys = resourceClass.getEnumConstants();
	}

	/**
	 * Constructor for explicit array of keys (non-enum resource).
	 */
	public LocaleResourceDefinition(final String name, final String baseName, final LocaleMessageKey[] keys) {
		super(name);
		Assertion.check()
				.isNotBlank(baseName)
				.isNotNull(keys);
		this.baseName = baseName;
		this.keys = keys;
	}

	public String getBaseName() {
		return baseName;
	}

	public LocaleMessageKey[] getKeys() {
		return keys;
	}
}
