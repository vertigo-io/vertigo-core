/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store.criteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.vertigo.dynamo.domain.metamodel.DtFieldName;

public final class CriteriaCtx {
	private int i;
	private final Map<String, Object> attributeValues = new HashMap<>();
	private final Map<String, DtFieldName> attributeNames = new HashMap<>();

	String attributeName(final DtFieldName dtFieldName, final Object value) {
		final String attributeName = dtFieldName.name() + '_' + i;
		i++;
		attributeValues.put(attributeName, value);
		attributeNames.put(attributeName, dtFieldName);
		return attributeName;
	}

	public Set<String> getAttributeNames() {
		return attributeValues.keySet();
	}

	public DtFieldName getDtFieldName(final String attributeName) {
		return attributeNames.get(attributeName);
	}

	public Object getAttributeValue(final String attributeName) {
		return attributeValues.get(attributeName);
	}
}
