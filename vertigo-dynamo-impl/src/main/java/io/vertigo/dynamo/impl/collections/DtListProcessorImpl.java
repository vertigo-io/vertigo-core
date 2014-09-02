package io.vertigo.dynamo.impl.collections;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListChainFilter;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class DtListProcessorImpl implements DtListProcessor {
	private final List<DtListFunction> listFunctions = new ArrayList<>();
	private final PersistenceManager persistenceManager;
	private final Option<IndexPlugin> indexPlugin;

	DtListProcessorImpl(final PersistenceManager persistenceManager, final Option<IndexPlugin> indexPlugin) {
		Assertion.checkNotNull(persistenceManager);
		Assertion.checkNotNull(indexPlugin);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
		this.indexPlugin = indexPlugin;
	}

	/** {@inheritDoc} */
	public DtListProcessor filter(final String keywords, final int maxRows, final Collection<DtField> searchedFields) {
		Assertion.checkArgument(indexPlugin.isDefined(), "An IndexPlugin is required to use this method");
		//---------------------------------------------------------------------
		listFunctions.add(new FullTextFilterFunction<>(keywords, maxRows, searchedFields, indexPlugin.get()));
		return this;
	}

	/** {@inheritDoc} */
	public DtListProcessor sort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		final SortState sortState = new SortState(fieldName, desc, nullLast, ignoreCase);
		listFunctions.add(new SortFunction<>(sortState, persistenceManager));
		return this;
	}

	/** {@inheritDoc} */
	public DtListProcessor filterByValue(final String fieldName, final Serializable value) {
		final DtListFilter filter = new DtListValueFilter(fieldName, value);
		listFunctions.add(new FilterFunction(filter));
		return this;

	}

	/** {@inheritDoc} */
	public DtListProcessor filterByTwoValues(final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2) {
		final DtListFilter filter1 = new DtListValueFilter<>(fieldName1, value1);
		final DtListFilter filter2 = new DtListValueFilter<>(fieldName2, value2);
		final DtListFilter filter = new DtListChainFilter<>(filter1, filter2);
		listFunctions.add(new FilterFunction<>(filter));
		return this;

	}

	/** {@inheritDoc} */
	public DtListProcessor filter(final ListFilter listFilter) {
		final DtListFilter filter = new DtListPatternFilter<>(listFilter.getFilterValue());
		listFunctions.add(new FilterFunction<>(filter));
		return this;
	}

	/** {@inheritDoc} */
	public DtListProcessor filterSubList(final int start, final int end) {
		listFunctions.add(new SubListFunction<>(start, end));
		return this;
	}

	/** {@inheritDoc} */
	public <C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max) {
		final DtListFilter filter = new DtListRangeFilter(fieldName, min, max, true, true);
		listFunctions.add(new FilterFunction<>(filter));
		return this;
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
