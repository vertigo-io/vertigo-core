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

import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListPatternFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.dynamo.impl.collections.functions.fulltext.FullTextFilterFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortFunction;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.dynamo.impl.collections.functions.sublist.SubListFunction;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

final class DtListProcessorImpl implements DtListProcessor {
	private final DtListFunction[] listFunctions;
	private final Option<IndexPlugin> indexPlugin;

	DtListProcessorImpl( final Option<IndexPlugin> indexPlugin) {
		this( new DtListFunction[]{},  indexPlugin);
	}

	// Getteur sur Home car dépendance cyclique entre CollectionsManager et PersistenceManager
	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
	}
	private DtListProcessorImpl(final DtListFunction[] listFunctions, final Option<IndexPlugin> indexPlugin) {
		Assertion.checkNotNull(listFunctions);
		Assertion.checkNotNull(indexPlugin);
		//---------------------------------------------------------------------
		this.listFunctions=listFunctions;
		this.indexPlugin = indexPlugin;
	}

	private DtListProcessorImpl createNewDtListProcessor(final DtListFunction listFunction){
		Assertion.checkNotNull(listFunction);
		//---------------------------------------------------------------------
		final DtListFunction[] list = Arrays.copyOf(listFunctions, listFunctions.length+1);
		//adding a new listFunction
		list[listFunctions.length]=listFunction;
		return new DtListProcessorImpl(list,  indexPlugin);
	}

	/** {@inheritDoc} */
	public DtListProcessor add(final DtListFunction listFunction) {
		return createNewDtListProcessor(listFunction);
	}

	/** {@inheritDoc} */
	public DtListProcessor filter(final String keywords, final int maxRows, final Collection<DtField> searchedFields) {
		Assertion.checkArgument(indexPlugin.isDefined(), "An IndexPlugin is required to use this method");
		//---------------------------------------------------------------------
		return add(new FullTextFilterFunction<>(keywords, maxRows, searchedFields, indexPlugin.get()));
	}

	/** {@inheritDoc} */
	public DtListProcessor sort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		final SortState sortState = new SortState(fieldName, desc, nullLast, ignoreCase);
		return add(new SortFunction<>(sortState, getPersistenceManager()));
	}

	/** {@inheritDoc} */
	public DtListProcessor filterByValue(final String fieldName, final Serializable value) {
		final DtListFilter filter = new DtListValueFilter(fieldName, value);
		return add(new FilterFunction(filter));
	}

	/** {@inheritDoc} */
	public DtListProcessor filter(final ListFilter listFilter) {
		final DtListFilter filter = new DtListPatternFilter<>(listFilter.getFilterValue());
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	public DtListProcessor filterSubList(final int start, final int end) {
		return add(new SubListFunction<>(start, end));
	}

	/** {@inheritDoc} */
	public <C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max) {
		final DtListFilter filter = new DtListRangeFilter(fieldName, min, max, true, true);
		return add(new FilterFunction<>(filter));
	}

	/** {@inheritDoc} */
	public <D extends DtObject> DtList<D> apply(final DtList<D> input) {
		Assertion.checkNotNull(input);
		//-------------------------------------------------------------
		DtList<D> current = input;
		for (final DtListFunction<D> listFunction : listFunctions) {
			current = listFunction.apply(current);
		}
		return current;
	}
}
