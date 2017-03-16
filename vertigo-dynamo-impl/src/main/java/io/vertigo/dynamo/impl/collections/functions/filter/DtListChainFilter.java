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
package io.vertigo.dynamo.impl.collections.functions.filter;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

/**
 * Chainage de filtre de liste.
 * Les filtres sont evalués dans l'ordre, il est préférable de mettre les plus discriminant en premier.
 * @author pchretien
 * @param <D> Type d'objet
 */
public final class DtListChainFilter<D extends DtObject> implements Predicate<D>, Serializable {
	private static final long serialVersionUID = -81683701282488344L;
	private final Predicate<D>[] filters;

	/**
	 * Constructor.
	 * @param filters Liste des filtres.
	 */
	public DtListChainFilter(final Predicate<D>... filters) {
		Assertion.checkNotNull(filters);
		Assertion.checkArgument(filters.length > 0, "Il faut au moins un filter");
		//-----
		this.filters = filters;
	}

	/** {@inheritDoc} */
	@Override
	public boolean test(final D dto) {
		for (final Predicate<D> filter : filters) {
			if (!filter.test(dto)) {
				return false;
			}
		}
		return true;
	}
}
