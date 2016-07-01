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
package io.vertigo.dynamo.impl.collections.functions.filter;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Fonction de filtre.
 * @author pchretien
 */
public final class FilterFunction<D extends DtObject> implements DtListFunction<D> {
	private final DtListFilter<D> filter;

	public FilterFunction(final DtListFilter<D> filter) {
		Assertion.checkNotNull(filter);
		//-----
		this.filter = filter;
	}

	@Override
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		//-----
		final DtList<D> filteredDtc = new DtList<>(dtc.getDefinition());
		for (final D dto : dtc) {
			if (filter.accept(dto)) {
				filteredDtc.add(dto);
			}
		}
		return filteredDtc;
	}
}
