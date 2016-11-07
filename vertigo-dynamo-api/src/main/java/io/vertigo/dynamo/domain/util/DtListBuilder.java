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
package io.vertigo.dynamo.domain.util;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * This builder allows you to build a DtList using fluent style.
 * @author pchretien
 *
 * @param <D> the type of objects in this list
 */
public final class DtListBuilder<D extends DtObject> implements Builder<DtList<D>> {
	private final DtList<D> list;

	/**
	 * Constructor.
	 * @param dtObjectClass the type of the object, defined by its class
	 */
	public DtListBuilder(final Class<D> dtObjectClass) {
		Assertion.checkNotNull(dtObjectClass);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectClass);
		list = new DtList(dtDefinition);
	}

	/**
	 * Constructor.
	 * @param dtDefinition type of the object, defined by its definition
	 */
	public DtListBuilder(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		list = new DtList(dtDefinition);
	}

	/**
	 * Adds an new object
	 * @param value object
	 * @return this builder
	 */
	public DtListBuilder<D> add(final D value) {
		Assertion.checkNotNull(value);
		//-----
		list.add(value);
		return this;
	}

	/**
	 * Adds a list of objects
	 * @param values list of objects
	 * @return this builder
	 */
	public DtListBuilder<D> addAll(final DtList<D> values) {
		Assertion.checkNotNull(values);
		//-----
		list.addAll(values);
		return this;
	}

	@Override
	public DtList<D> build() {
		return list;
	}
}
