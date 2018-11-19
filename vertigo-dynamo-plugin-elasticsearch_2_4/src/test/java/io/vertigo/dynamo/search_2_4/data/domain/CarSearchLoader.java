/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.search_2_4.data.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.UID;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamox.search.AbstractSearchLoader;
import io.vertigo.lang.Assertion;

public final class CarSearchLoader extends AbstractSearchLoader<Long, Car, Car> {
	private static final int SEARCH_CHUNK_SIZE = 5;
	private final SearchManager searchManager;
	private CarDataBase carDataBase;

	@Inject
	public CarSearchLoader(final SearchManager searchManager) {
		Assertion.checkNotNull(searchManager);
		//---
		this.searchManager = searchManager;
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
	public List<SearchIndex<Car, Car>> loadData(final SearchChunk<Car> searchChunk) {
		Assertion.checkNotNull(carDataBase, "carDataBase not bound");
		//-----
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Car.class);
		final List<SearchIndex<Car, Car>> carIndexes = new ArrayList<>();
		final Map<Long, Car> carPerId = new HashMap<>();
		for (final Car car : carDataBase.getAllCars()) {
			carPerId.put(car.getId(), car);
		}
		for (final UID<Car> uri : searchChunk.getAllURIs()) {
			final Car car = carPerId.get(uri.getId());
			carIndexes.add(SearchIndex.createIndex(indexDefinition, uri, car));
		}
		return carIndexes;
	}

	/** {@inheritDoc} */
	@Override
	protected List<UID<Car>> loadNextURI(final Long lastId, final DtDefinition dtDefinition) {
		final SearchIndexDefinition indexDefinition = searchManager.findFirstIndexDefinitionByKeyConcept(Car.class);
		final List<UID<Car>> uris = new ArrayList<>(SEARCH_CHUNK_SIZE);
		//call loader service
		int i = 0;
		for (final Car car : carDataBase.getAllCars()) {
			if (i > lastId) {
				uris.add(UID.of(indexDefinition.getKeyConceptDtDefinition(), car.getId()));
			}
			if (uris.size() >= SEARCH_CHUNK_SIZE) {
				break;
			}
			i++;
		}
		return uris;
	}
}
