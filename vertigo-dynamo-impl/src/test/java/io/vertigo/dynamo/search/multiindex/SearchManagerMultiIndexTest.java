/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Item;
import io.vertigo.dynamo.search.data.domain.ItemDataBase;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.search.model.SearchQuery;

/**
 * @author  npiedeloup
 */
public class SearchManagerMultiIndexTest extends AbstractTestCaseJU4 {
	//Index
	private static final String IDX_DYNA_ITEM = "IDX_DYNA_ITEM";
	private static final String IDX_ITEM = "IDX_ITEM";

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;

	private ItemDataBase carDataBase;

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		carDataBase = new ItemDataBase();
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction mais sur deux indexes.
	 * Vérifie la capacité du système à gérer plusieurs indexes.
	 */
	@Test
	public void testIndex() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final SearchIndexDefinition carIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);
		final SearchIndexDefinition carDynIndexDefinition = definitionSpace.resolve(IDX_DYNA_ITEM, SearchIndexDefinition.class);

		for (final Item car : carDataBase.getAllItems()) {
			final SearchIndex<Item, Item> index = SearchIndex.createIndex(carIndexDefinition, car.getURI(), car);
			searchManager.put(carIndexDefinition, index);

			final SearchIndex<Item, Item> index2 = SearchIndex.createIndex(carDynIndexDefinition, car.getURI(), car);
			searchManager.put(carDynIndexDefinition, index2);
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
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final SearchIndexDefinition carIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);
		final SearchIndexDefinition carDynIndexDefinition = definitionSpace.resolve(IDX_DYNA_ITEM, SearchIndexDefinition.class);
		final ListFilter removeQuery = ListFilter.of("*:*");
		searchManager.removeAll(carIndexDefinition, removeQuery);
		searchManager.removeAll(carDynIndexDefinition, removeQuery);
		waitIndexation();

		final long sizeCar = query("*:*", carIndexDefinition);
		Assert.assertEquals(0, sizeCar);

		final long sizeCarDyn = query("*:*", carDynIndexDefinition);
		Assert.assertEquals(0, sizeCarDyn);
	}

	private long query(final String query, final SearchIndexDefinition indexDefinition) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(ListFilter.of(query))
				.build();
		final FacetedQueryResult<DtObject, SearchQuery> result = searchManager.loadList(indexDefinition, searchQuery, null);
		return result.getCount();
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(2000); //wait index was done
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}
}
