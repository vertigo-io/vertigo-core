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
package io.vertigo.dynamo.impl.collections;

import java.io.Serializable;
import java.util.Arrays;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortFunction;
import io.vertigo.dynamo.impl.collections.functions.sublist.SubListFunction;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

/**
 * Standard implementation of DtListProcessor.
 */
final class DtListProcessorImpl implements DtListProcessor {
	private final DtListFunction[] listFunctions;

	DtListProcessorImpl() {
		this(new DtListFunction[] {});
	}

	private DtListProcessorImpl(final DtListFunction[] listFunctions) {
		Assertion.checkNotNull(listFunctions);
		//-----
		this.listFunctions = listFunctions;
	}

	// Getteur sur Home car dépendance cyclique entre CollectionsManager et StoreManager
	private static StoreManager getStoreManager() {
		return Home.getApp().getComponentSpace().resolve(StoreManager.class);
	}

	private DtListProcessorImpl createNewDtListProcessor(final DtListFunction listFunction) {
		Assertion.checkNotNull(listFunction);
		//-----
		final DtListFunction[] list = Arrays.copyOf(listFunctions, listFunctions.length + 1);
		//adding a new listFunction
		list[listFunctions.length] = listFunction;
		return new DtListProcessorImpl(list);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor add(final DtListFunction listFunction) {
		return createNewDtListProcessor(listFunction);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor sort(final String fieldName, final boolean desc) {
		return add(new SortFunction<>(fieldName, desc, getStoreManager()));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filterByValue(final String fieldName, final Serializable value) {
		final DtListFilter filter = new DtListValueFilter(fieldName, value);
		return add(new FilterFunction(filter));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filter(final ListFilter listFilter) {
		final DtListFilter filter = new DtListPatternFilter<>(listFilter.getFilterValue());
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filterSubList(final int start, final int end) {
		return add(new SubListFunction<>(start, end));
	}

	/** {@inheritDoc} */
	@Override
	public <C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max) {
		final DtListFilter filter = new DtListRangeFilter(fieldName, min, max, true, true);
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> apply(final DtList<D> input) {
		Assertion.checkNotNull(input);
		//-----
		DtList<D> current = input;
		for (final DtListFunction<D> listFunction : listFunctions) {
			current = listFunction.apply(current);
		}
		return current;
	}
}
