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
package io.vertigo.dynamo.impl.collections.functions.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

/**
 * Fonction de tri.
 * @author pchretien
 */
public final class SortFunction<D extends DtObject> implements UnaryOperator<DtList<D>> {
	private final String sortFieldName;
	private final boolean sortDesc;
	private final StoreManager storeManager;

	public SortFunction(final String sortFieldName, final boolean sortDesc, final StoreManager storeManager) {
		Assertion.checkNotNull(storeManager);
		Assertion.checkArgNotEmpty(sortFieldName);
		//-----
		this.sortFieldName = sortFieldName;
		this.sortDesc = sortDesc;
		this.storeManager = storeManager;
	}

	@Override
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		//-----
		//On crée une liste triable par l'utilitaire java.util.Collections
		final List<D> list = new ArrayList<>(dtc);

		//On trie.
		final Comparator<D> comparator = new DtObjectComparator<>(storeManager, dtc.getDefinition(), sortFieldName, sortDesc);
		Collections.sort(list, comparator);

		//On reconstitue la collection.
		final DtList<D> sortedDtc = new DtList<>(dtc.getDefinition());
		sortedDtc.addAll(list);
		return sortedDtc;
	}
}
