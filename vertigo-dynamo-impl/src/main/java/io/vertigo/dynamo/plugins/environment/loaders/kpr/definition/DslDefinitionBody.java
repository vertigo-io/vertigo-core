/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import java.util.List;

import io.vertigo.lang.Assertion;

public final class DslDefinitionBody {
	private final List<DslDefinitionEntry> definitionEntries;
	private final List<DslPropertyEntry> propertyEntries;

	public DslDefinitionBody(final List<DslDefinitionEntry> definitionEntries, final List<DslPropertyEntry> propertyEntries) {
		Assertion.checkNotNull(definitionEntries);
		Assertion.checkNotNull(propertyEntries);
		//-----
		this.definitionEntries = definitionEntries;
		this.propertyEntries = propertyEntries;
	}

	public List<DslPropertyEntry> getPropertyEntries() {
		return propertyEntries;
	}

	public List<DslDefinitionEntry> getDefinitionEntries() {
		return definitionEntries;
	}
}
