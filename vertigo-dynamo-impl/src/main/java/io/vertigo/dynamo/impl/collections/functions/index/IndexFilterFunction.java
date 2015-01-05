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
package io.vertigo.dynamo.impl.collections.functions.index;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * List Function powered with index engine.
 * @author npiedeloup (5 janv. 2015 10:47:08)
 * @param <D> Object type
 */
public final class IndexFilterFunction<D extends DtObject> implements DtListFunction<D> {

	private String keywords;
	private Collection<DtField> searchedFields;
	private final List<ListFilter> listFilters = new ArrayList<>();
	private int skip = 0;
	private int top = 250;
	private SortState sortState;

	private final IndexPlugin indexPlugin;

	/**
	 * Constructor.
	 * @param indexPlugin Index plugin
	 */
	public IndexFilterFunction(final IndexPlugin indexPlugin) {
		Assertion.checkNotNull(indexPlugin, "An IndexPlugin is required to use this method");
		//-----
		this.indexPlugin = indexPlugin;
	}

	/**
	 * Set filter by keywords.
	 * @param userKeywords user keywords
	 * @param maxRows Max rows
	 * @param keywordsSearchedFields searched fields
	 */
	public void filter(final String userKeywords, final int maxRows, final Collection<DtField> keywordsSearchedFields) {
		Assertion.checkState(keywords == null, "Keywords was already set on this processor : {0}. Only one is supported.", keywords);
		Assertion.checkNotNull(userKeywords);
		//-----
		keywords = userKeywords;
		top = maxRows;
		searchedFields = keywordsSearchedFields;
	}

	/**
	 * Set sort directives.
	 * Some directives can't be realized.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @param nullLast Si les objets Null sont en derniers
	 * @param ignoreCase Si on ignore la casse
	 */
	public void sort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		sortState = new SortState(fieldName, desc, nullLast, ignoreCase);
	}

	/**
	 * Add a listfilter (check ListFilter syntax).
	 * @param listFilter ListFilter
	 */
	public void filter(final ListFilter listFilter) {
		listFilters.add(listFilter);
	}

	/**
	 * Set sublist filter.
	 * @param start first index
	 * @param end last index
	 */
	public void filterSubList(final int start, final int end) {
		skip = start;
		top = end - start;
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		//-----
		return indexPlugin.getCollection(keywords, searchedFields, listFilters, skip, top, Option.option(sortState), Option.<DtField> none(), dtc);
	}
}
