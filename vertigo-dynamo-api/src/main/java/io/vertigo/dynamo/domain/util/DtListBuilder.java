/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
 * @author pchretien
 */
public final class DtListBuilder<X extends DtObject> implements Builder<DtList<X>> {
	private final DtList<X> list;

	public DtListBuilder(final Class<X> dtObjectClass) {
		Assertion.checkNotNull(dtObjectClass);
		//-----
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectClass);
		list = new DtList(dtDefinition);
	}

	public DtListBuilder(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//-----
		list = new DtList(dtDefinition);
	}

	public DtListBuilder<X> add(final X value) {
		Assertion.checkNotNull(value);
		//-----
		list.add(value);
		return this;
	}

	public DtListBuilder<X> addAll(final DtList<X> values) {
		Assertion.checkNotNull(values);
		//-----
		list.addAll(values);
		return this;
	}

	//	public DtListBuilder<X> unmodifiable() {
	//		this.list = Collections.unmodifiableList(list);
	//		return this;
	//	}

	@Override
	public DtList<X> build() {
		return list;
	}
}
