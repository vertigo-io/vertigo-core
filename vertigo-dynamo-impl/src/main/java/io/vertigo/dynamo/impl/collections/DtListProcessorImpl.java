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
package io.vertigo.dynamo.impl.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import io.vertigo.app.Home;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortFunction;
import io.vertigo.dynamo.impl.collections.functions.sublist.SubListFunction;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Standard implementation of DtListProcessor.
 */
final class DtListProcessorImpl<D extends DtObject> implements DtListProcessor<D> {
	private final List<Function<DtList<D>, DtList<D>>> listFunctions;

	DtListProcessorImpl() {
		listFunctions = new ArrayList<>();
	}

	private DtListProcessorImpl(final List<Function<DtList<D>, DtList<D>>> listFunctions) {
		Assertion.checkNotNull(listFunctions);
		//-----
		this.listFunctions = listFunctions;
	}

	// Getteur sur Home car dépendance cyclique entre CollectionsManager et StoreManager
	private static StoreManager getStoreManager() {
		return Home.getApp().getComponentSpace().resolve(StoreManager.class);
	}

	private DtListProcessorImpl<D> createNewDtListProcessor(final Function<DtList<D>, DtList<D>> listFunction) {
		Assertion.checkNotNull(listFunction);
		return new DtListProcessorImpl<>(new ListBuilder<Function<DtList<D>, DtList<D>>>().addAll(listFunctions).add(listFunction).unmodifiable().build());
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor<D> add(final UnaryOperator<DtList<D>> listFunction) {
		return createNewDtListProcessor(listFunction);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor<D> sort(final String fieldName, final boolean desc) {
		return add(new SortFunction<>(fieldName, desc, getStoreManager()));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor<D> filterByValue(final String fieldName, final Serializable value) {
		final Predicate<D> filter = new DtListValueFilter<>(fieldName, value);
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor<D> filter(final ListFilter listFilter) {
		final Predicate<D> filter = new DtListPatternFilter<>(listFilter.getFilterValue());
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor<D> filterSubList(final int start, final int end) {
		return add(new SubListFunction<D>(start, end));
	}

	/** {@inheritDoc} */
	@Override
	public <C extends Comparable<?>> DtListProcessor<D> filterByRange(final String fieldName, final Optional<C> min, final Optional<C> max) {
		final Predicate<D> filter = new DtListRangeFilter<>(fieldName, min, max, true, true);
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> apply(final DtList<D> input) {
		Assertion.checkNotNull(input);
		//-----
		return listFunctions
				.stream()
				.reduce(UnaryOperator.identity(), (fun1, fun2) -> fun1.andThen(fun2))
				.apply(input);
	}
}
