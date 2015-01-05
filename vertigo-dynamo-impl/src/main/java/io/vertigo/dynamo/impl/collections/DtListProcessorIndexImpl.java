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

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.functions.index.IndexFilterFunction;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.Serializable;
import java.util.Collection;

/**
 * Implementation of DtListProcessor with Index usage.
 * Can cumulate index compatible functions.
 * When added a index non-compatible functions will fallback to standard implementation.
 */
final class DtListProcessorIndexImpl implements DtListProcessor {

	private final DtListProcessor subDtListProcessor;
	private final IndexFilterFunction indexFilterFunction;

	DtListProcessorIndexImpl(final DtListProcessor subDtListProcessor, final IndexPlugin indexPlugin) {
		Assertion.checkNotNull(indexPlugin, "An IndexPlugin is required to use this method");
		//-----
		indexFilterFunction = new IndexFilterFunction(indexPlugin);
		this.subDtListProcessor = subDtListProcessor.add(indexFilterFunction);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor add(final DtListFunction listFunction) {
		return subDtListProcessor.add(listFunction);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filter(final String userKeywords, final int maxRows, final Collection<DtField> keywordsSearchedFields) {
		indexFilterFunction.filter(userKeywords, maxRows, keywordsSearchedFields);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor sort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase) {
		indexFilterFunction.sort(fieldName, desc, nullLast, ignoreCase);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filterByValue(final String fieldName, final Serializable value) {
		if (value instanceof String) {
			final String escapedValue = ((String) value).replace("\"", "\\\"");
			final ListFilter listFilter = new ListFilter(fieldName + ":\"" + escapedValue + "\"");
			indexFilterFunction.filter(listFilter);
			return this;
		}
		return subDtListProcessor.filterByValue(fieldName, value);
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filter(final ListFilter listFilter) {
		indexFilterFunction.filter(listFilter);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public DtListProcessor filterSubList(final int start, final int end) {
		indexFilterFunction.filterSubList(start, end);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public <C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max) {
		return subDtListProcessor.filterByRange(fieldName, min, max);
	}

	/** {@inheritDoc} */
	@Override
	public <D extends DtObject> DtList<D> apply(final DtList<D> input) {
		return subDtListProcessor.apply(input);
	}
}
