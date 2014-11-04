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
package io.vertigo.dynamo.search.multiindex;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.model.FacetedQuery;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 */
public final class SearchManagerMultiIndexTest extends AbstractTestCaseJU4 {
	//Index
	private static final String IDX_DYNA_CAR = "IDX_DYNA_CAR";
	private static final String IDX_CAR = "IDX_CAR";
	//Query sans facette
	private static final String QRY_CAR = "QRY_CAR";

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;

	private CarDataBase carDataBase;

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		carDataBase = new CarDataBase();
		carDataBase.loadDatas();

		final Map<String, String> indexFieldsMap = new HashMap<>();
		final IndexDefinition carDynIndexDefinition = Home.getDefinitionSpace().resolve(IDX_DYNA_CAR, IndexDefinition.class);
		for (final DtField dtField : carDynIndexDefinition.getIndexDtDefinition().getFields()) {
			String indexType = dtField.getDomain().getProperties().getValue(DtProperty.INDEX_TYPE);
			if (indexType == null) {
				indexType = dtField.getDomain().getDataType().name().toLowerCase();
			}
			indexFieldsMap.put(dtField.getName(), dtField.getName() + "_DYN" + indexType);
		}
		searchManager.getSearchServices().registerIndexFieldNameResolver(carDynIndexDefinition, new IndexFieldNameResolver(indexFieldsMap));
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction mais sur deux indexes.
	 * Vérifie la capacité du système à gérer plusieurs indexes.
	 */
	@Test
	public void testIndex() {
		final IndexDefinition carIndexDefinition = Home.getDefinitionSpace().resolve(IDX_CAR, IndexDefinition.class);
		final IndexDefinition carDynIndexDefinition = Home.getDefinitionSpace().resolve(IDX_DYNA_CAR, IndexDefinition.class);

		for (final Car car : carDataBase) {
			final Index<Car, Car> index = Index.createIndex(carIndexDefinition, createURI(car), car, car);
			searchManager.getSearchServices().put(carIndexDefinition, index);

			final Index<Car, Car> index2 = Index.createIndex(carDynIndexDefinition, createURI(car), car, car);
			searchManager.getSearchServices().put(carDynIndexDefinition, index2);
		}
		waitIndexation();

		final long sizeCar = query("*:*", carIndexDefinition);
		Assert.assertEquals(carDataBase.size(), sizeCar);

		final long sizeCarDyn = query("*:*", carDynIndexDefinition);
		Assert.assertEquals(carDataBase.size(), sizeCarDyn);
	}

	/**
	 * Test de création nettoyage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testClean() {
		final IndexDefinition carIndexDefinition = Home.getDefinitionSpace().resolve(IDX_CAR, IndexDefinition.class);
		final IndexDefinition carDynIndexDefinition = Home.getDefinitionSpace().resolve(IDX_DYNA_CAR, IndexDefinition.class);
		final ListFilter removeQuery = new ListFilter("*:*");
		searchManager.getSearchServices().remove(carIndexDefinition, removeQuery);
		searchManager.getSearchServices().remove(carDynIndexDefinition, removeQuery);
	}

	private long query(final String query, final IndexDefinition indexDefinition) {
		//recherche
		final FacetedQueryDefinition carQueryDefinition = Home.getDefinitionSpace().resolve(QRY_CAR, FacetedQueryDefinition.class);
		final ListFilter listFilter = new ListFilter(query);
		final SearchQuery searchQuery = SearchQuery.createSearchQuery(indexDefinition, listFilter);
		final FacetedQuery facetedQuery = new FacetedQuery(carQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<DtObject, SearchQuery> result = searchManager.getSearchServices().loadList(searchQuery, facetedQuery);
		return result.getCount();
	}

	private static URI<Car> createURI(final Car car) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		return new URI<>(dtDefinition, DtObjectUtil.getId(car));
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(1000); //wait index was done
		} catch (final InterruptedException e) {
			//rien
		}
	}
}
