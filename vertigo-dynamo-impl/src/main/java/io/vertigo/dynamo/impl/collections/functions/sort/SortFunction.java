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
package io.vertigo.dynamo.impl.collections.functions.sort;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Fonction de tri.
 * @author pchretien
 */
public final class SortFunction<D extends DtObject> implements DtListFunction<D> {
	private final SortState sortState;
	private final PersistenceManager persistenceManager;

	public SortFunction(final SortState sortState, final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(persistenceManager);
		Assertion.checkNotNull(sortState);
		//-----
		this.sortState = sortState;
		this.persistenceManager = persistenceManager;
	}

	@Override
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		Assertion.checkNotNull(sortState);
		//-----
		//On crée une liste triable par l'utilitaire java.util.Collections
		final List<D> list = new ArrayList<>(dtc);

		//On trie.
		final Comparator<D> comparator = new DtObjectComparator<>(persistenceManager, dtc.getDefinition(), sortState);
		Collections.sort(list, comparator);

		//On reconstitue la collection.
		final DtList<D> sortedDtc = new DtList<>(dtc.getDefinition());
		sortedDtc.addAll(list);
		return sortedDtc;
	}
}
