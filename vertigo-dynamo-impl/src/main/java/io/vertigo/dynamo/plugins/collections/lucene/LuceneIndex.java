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
package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.lang.Option;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Interface réprésentant un IndexLucene.
 *
 * @author npiedeloup
 * @param <D> type d'objet indexé
 */
public interface LuceneIndex<D extends DtObject> {

	/**
	 * Add element to index.
	 * @param fullDtc Full Dtc to index
	 * @param storeValue if data are store in index
	 * @throws IOException Indexation error
	 */
	void addAll(final DtList<D> fullDtc, final boolean storeValue) throws IOException;

	/**
	 * Querying index.
	 * @param keywords Keywords
	 * @param searchedFields Searched field list
	 * @param listFilters Added filters
	 * @param skip skip elements
	 * @param top max elements (max number of result)
	 * @param sortState Sort information
	 * @param boostedField Field use for boosting score
	 * @return Filtered ordered list
	 * @throws IOException Query error
	 */
	DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final int skip, final int top, final Option<SortState> sortState, final Option<DtField> boostedField) throws IOException;

}
