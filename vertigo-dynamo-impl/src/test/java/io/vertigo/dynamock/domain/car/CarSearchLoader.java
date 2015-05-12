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
package io.vertigo.dynamock.domain.car;

import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public final class CarSearchLoader implements SearchLoader<Car, Car, Car> {
	private static final int SEARCH_CHUNK_SIZE = 5;
	private final SearchIndexDefinition indexDefinition;
	private CarDataBase carDataBase;

	@Inject
	public CarSearchLoader(final SearchManager searchManager) {
		indexDefinition = searchManager.findIndexDefinitionBySubject(Car.class);
	}

	/**
	 * @param boundedDataBase Database to bound with this loader (specific for tests)
	 */
	public void bindDataBase(final CarDataBase boundedDataBase) {
		Assertion.checkNotNull(boundedDataBase);
		//----
		carDataBase = boundedDataBase;
	}

	/** {@inheritDoc} */
	@Override
	public List<SearchIndex<Car, Car, Car>> loadData(final List<URI<Car>> uris) {
		final List<SearchIndex<Car, Car, Car>> carIndexes = new ArrayList<>(uris.size());
		final Map<Long, Car> carPerId = new HashMap<>();
		for (final Car car : carDataBase) {
			carPerId.put(car.getId(), car);
		}
		for (final URI<Car> uri : uris) {
			final Car car = carPerId.get(uri.getId());
			carIndexes.add(SearchIndex.createIndex(indexDefinition, uri, car, car));
		}
		return carIndexes;
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<SearchChunk<Car>> chunk(final Class<Car> subjectClass) {

		return new Iterable<SearchChunk<Car>>() {
			private final Iterator<SearchChunk<Car>> iterator = new Iterator<SearchChunk<Car>>() {
				private SearchChunk<Car> current = null;

				@Override
				public boolean hasNext() {
					return hasNextChunk(subjectClass, current);
				}

				@Override
				public SearchChunk<Car> next() {
					final SearchChunk<Car> next = nextChunk(subjectClass, current);
					current = next;
					return current;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("This list is unmodifiable");
				}
			};

			@Override
			public Iterator<SearchChunk<Car>> iterator() {
				return iterator;
			}

		};
	}

	private SearchChunk<Car> nextChunk(final Class<Car> subjectClass, final SearchChunk<Car> previousChunck) {
		Long lastId = -1L;
		if (previousChunck != null) {
			final List<URI<Car>> previousUris = previousChunck.getAllURIs();
			Assertion.checkState(!previousUris.isEmpty(), "No more SearchChunk for DtSubject {0}, ensure you use Iterable pattern or call hasNext before next", subjectClass.getSimpleName());
			lastId = (Long) previousUris.get(previousUris.size() - 1).getId();
		}
		final List<URI<Car>> uris = new ArrayList<>(SEARCH_CHUNK_SIZE);
		//call loader service
		int i = 0;
		for (final Car car : carDataBase) {
			if (i > lastId) {
				uris.add(new URI(indexDefinition.getSubjectDtDefinition(), car.getId()));
			}
			if (uris.size() >= SEARCH_CHUNK_SIZE) {
				break;
			}
			i++;
		}
		return new SearchChunkImpl<>(uris);
	}

	private boolean hasNextChunk(final Class<Car> subjectClass, final SearchChunk<Car> previousChunck) {
		//il y a une suite, si on a pas commencé, ou s'il y avait des résultats la dernière fois.
		return previousChunck == null || !previousChunck.getAllURIs().isEmpty();
	}

	public static class SearchChunkImpl<S extends DtSubject> implements SearchChunk<S> {
		private final List<URI<S>> uris;

		/**
		 * @param uris Liste des uris du chunk
		 */
		public SearchChunkImpl(final List<URI<S>> uris) {
			Assertion.checkNotNull(uris);
			//----
			this.uris = Collections.unmodifiableList(uris); //pas de clone pour l'instant
		}

		/** {@inheritDoc} */
		@Override
		public List<URI<S>> getAllURIs() {
			return uris;
		}

	}
}
