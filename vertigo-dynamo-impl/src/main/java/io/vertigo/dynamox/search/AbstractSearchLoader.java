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
package io.vertigo.dynamox.search;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.lang.Assertion;

/**
 * Abstract SearchLoader with default chunk implementation.
 * @author npiedeloup
 * @param <P> Primary key type
 * @param <K> KeyConcept type
 * @param <I> Index type
 */
public abstract class AbstractSearchLoader<P extends Serializable, K extends KeyConcept, I extends DtObject> implements
		SearchLoader<K, I> {

	/** {@inheritDoc} */
	@Override
	public final Iterable<Optional<SearchChunk<K>>> chunk(final Class<K> keyConceptClass) {
		return new Iterable<Optional<SearchChunk<K>>>() {

			private final Iterator<Optional<SearchChunk<K>>> iterator = new Iterator<Optional<SearchChunk<K>>>() {

				private SearchChunk<K> current = null;

				/** {@inheritDoc} */
				@Override
				public boolean hasNext() {
					//hasNext at first call, and if current.allUri wasn't empty
					return current == null || !current.getAllURIs().isEmpty();
				}

				/** {@inheritDoc} */
				@Override
				public Optional<SearchChunk<K>> next() {
					current = nextChunk(keyConceptClass, current);
					if (current.getAllURIs().isEmpty()) {
						return Optional.empty();
					}
					return Optional.of(current);
				}

				/** {@inheritDoc} */
				@Override
				public void remove() {
					throw new UnsupportedOperationException("This list is unmodifiable");
				}
			};

			/** {@inheritDoc} */
			@Override
			public Iterator<Optional<SearchChunk<K>>> iterator() {
				return iterator;
			}
		};
	}

	private SearchChunk<K> nextChunk(final Class<K> keyConceptClass, final SearchChunk<K> previousChunck) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		P lastId = getLowestIdValue(dtDefinition);
		if (previousChunck != null) {
			final List<URI<K>> previousUris = previousChunck.getAllURIs();
			Assertion
					.checkState(
							!previousUris.isEmpty(),
							"No more SearchChunk for KeyConcept {0}, ensure you use Iterable pattern or call hasNext before next",
							keyConceptClass.getSimpleName());
			lastId = (P) previousUris.get(previousUris.size() - 1).getId();
		}
		// call loader service
		final List<URI<K>> uris = loadNextURI(lastId, dtDefinition);
		return new SearchChunk<>(uris);
	}

	/**
	 * Load uris of next chunk.
	 * @param lastId Last chunk id
	 * @param dtDefinition KeyConcept definition
	 * @return Uris of next chunk.
	 */
	protected abstract List<URI<K>> loadNextURI(final P lastId, final DtDefinition dtDefinition);

	private P getLowestIdValue(final DtDefinition dtDefinition) {
		final DtField idField = dtDefinition.getIdField().get();
		final DataType idDataType = idField.getDomain().getDataType();
		P pkValue;
		switch (idDataType) {
			case Integer:
				pkValue = (P) Integer.valueOf(-1);
				break;
			case Long:
				pkValue = (P) Long.valueOf(-1);
				break;
			case String:
				pkValue = (P) "";
				break;
			case BigDecimal:
			case DataStream:
			case Boolean:
			case Double:
			case Date:
			case DtList:
			case DtObject:
			default:
				throw new IllegalArgumentException("Type's PK " + idDataType.name() + " of "
						+ dtDefinition.getClassSimpleName() + " is not supported, prefer int, long or String ID.");
		}
		return pkValue;
	}
}
