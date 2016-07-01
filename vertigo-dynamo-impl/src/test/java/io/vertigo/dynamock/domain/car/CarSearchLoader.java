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
package io.vertigo.dynamock.domain.car;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamox.search.AbstractSearchLoader;
import io.vertigo.lang.Assertion;

public final class CarSearchLoader extends AbstractSearchLoader<Long, Car, Car> {
	private static final int SEARCH_CHUNK_SIZE = 5;
	private final SearchIndexDefinition indexDefinition;
	private CarDataBase carDataBase;
	private final VTransactionManager transactionManager;

	@Inject
	public CarSearchLoader(final SearchManager searchManager, final VTransactionManager transactionManager) {
		indexDefinition = searchManager.findIndexDefinitionByKeyConcept(Car.class);
		this.transactionManager = transactionManager;
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
	public List<SearchIndex<Car, Car>> loadData(final List<URI<Car>> uris) {
		Assertion.checkNotNull(carDataBase, "carDataBase not bound");
		Assertion.checkState(transactionManager.hasCurrentTransaction(), "SearchLoader must be use in Tx");
		//-----
		final List<SearchIndex<Car, Car>> carIndexes = new ArrayList<>(uris.size());
		final Map<Long, Car> carPerId = new HashMap<>();
		for (final Car car : carDataBase) {
			carPerId.put(car.getId(), car);
		}
		for (final URI<Car> uri : uris) {
			final Car car = carPerId.get(uri.getId());
			carIndexes.add(SearchIndex.createIndex(indexDefinition, uri, car));
		}
		return carIndexes;
	}

	/** {@inheritDoc} */
	@Override
	protected List<URI<Car>> loadNextURI(final Long lastId, final DtDefinition dtDefinition) {
		Assertion.checkState(transactionManager.hasCurrentTransaction(), "SearchLoader must be use in Tx");
		//-----
		final List<URI<Car>> uris = new ArrayList<>(SEARCH_CHUNK_SIZE);
		//call loader service
		int i = 0;
		for (final Car car : carDataBase) {
			if (i > lastId) {
				uris.add(new URI(indexDefinition.getKeyConceptDtDefinition(), car.getId()));
			}
			if (uris.size() >= SEARCH_CHUNK_SIZE) {
				break;
			}
			i++;
		}
		return uris;
	}
}
