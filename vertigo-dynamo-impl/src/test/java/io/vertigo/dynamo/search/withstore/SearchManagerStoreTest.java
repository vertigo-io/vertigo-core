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
package io.vertigo.dynamo.search.withstore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;
import io.vertigo.database.sql.statement.SqlStatement;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Item;
import io.vertigo.dynamo.search.data.domain.ItemDataBase;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 * Test de l'implémentation standard couplé au store.
 *
 * @author npiedeloup
 */
public class SearchManagerStoreTest extends AbstractTestCaseJU4 {
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private SearchManager searchManager;
	//Index
	private static final String IDX_ITEM = "IDX_ITEM";

	private SearchIndexDefinition itemIndexDefinition;

	private long initialDbItemSize = 0;

	@Override
	protected void doSetUp() throws Exception {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		itemIndexDefinition = definitionSpace.resolve(IDX_ITEM, SearchIndexDefinition.class);

		//A chaque test on recrée la table famille
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
			execCallableStatement(connection, "create table item(ID BIGINT, MANUFACTURER varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50), OPTIONAL_NUMBER BIGINT, OPTIONAL_STRING varchar(50), LAST_MODIFIED timestamp );");
			execCallableStatement(connection, "create sequence SEQ_ITEM start with 10001 increment by 1");
		}

		//On supprime tout
		remove("*:*");

		final ItemDataBase itemDataBase = new ItemDataBase();
		initialDbItemSize = itemDataBase.size();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Item item : itemDataBase.getAllItems()) {
				item.setId(null);
				storeManager.getDataStore().create(item);
			}
			transaction.commit();
		}
		waitIndexation();
	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws Exception {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//A chaque fin de test on arréte la base.
			final SqlConnection connection = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
			execCallableStatement(connection, "shutdown;");
			transaction.commit();
		}
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		dataBaseManager.executeUpdate(
				SqlStatement.builder(sql).build(),
				connection);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		final long size = query("*:*");
		Assert.assertEquals(initialDbItemSize, size);
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexNewData() {
		testIndexAllQuery();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Item item = createNewItem();
			storeManager.getDataStore().create(item);
			transaction.commit();
		}
		waitIndexation();
		Assert.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assert.assertEquals(1, query("DESCRIPTION:légende"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDeleteData() {
		testIndexAllQuery();
		Assert.assertEquals(1, query("ID:10001"));

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().delete(createURI(10001L));
			transaction.commit();
		}
		waitIndexation();
		Assert.assertEquals(0, query("ID:10001"));
		Assert.assertEquals(initialDbItemSize - 1, query("*:*"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(item);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assert.assertEquals(1, query("DESCRIPTION:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().update(item);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assert.assertEquals(0, query("DESCRIPTION:légende"));
		Assert.assertEquals(1, query("DESCRIPTION:vendue"));
	}

	/**
	 * Test de mise à jour de l'index après une creation.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexDoubleUpdateData() {
		testIndexAllQuery();
		final Item item = createNewItem();

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(item);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assert.assertEquals(1, query("DESCRIPTION:légende"));

		item.setDescription("Vendue");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			storeManager.getDataStore().update(item);
			transaction.commit();
		}

		waitIndexation();
		Assert.assertEquals(initialDbItemSize + 1, query("*:*"));
		Assert.assertEquals(0, query("DESCRIPTION:légende"));
		Assert.assertEquals(1, query("DESCRIPTION:vendue"));
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		//On supprime tout
		remove("*:*");
		final long resize = query("*:*");
		Assert.assertEquals(0L, resize);
	}

	private static Item createNewItem() {
		final Item item = new Item();
		item.setId(null);
		item.setPrice(12000);
		item.setManufacturer("Acme");
		item.setModel("Martin");
		item.setYear(1978);
		item.setKilo(1500);
		final BigDecimal conso = new BigDecimal(7.6);
		conso.setScale(2, RoundingMode.HALF_UP);
		item.setConsommation(conso);
		item.setMotorType("essence");
		item.setDescription("Voiture de légende assurant une reindexation dès son insertion");
		return item;
	}

	private long query(final String query) {
		//recherche
		final SearchQuery searchQuery = SearchQuery.builder(ListFilter.of(query))
				.build();

		return doQuery(searchQuery, null).getCount();
	}

	private FacetedQueryResult<Item, SearchQuery> doQuery(final SearchQuery searchQuery, final DtListState listState) {
		return searchManager.loadList(itemIndexDefinition, searchQuery, listState);
	}

	private static URI createURI(final long itemId) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Item.class);
		return new URI(dtDefinition, itemId);
	}

	protected void remove(final String query) {
		doRemove(query);
		waitIndexation();
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = ListFilter.of(query);
		searchManager.removeAll(itemIndexDefinition, removeQuery);
	}

	private static void waitIndexation() {
		try {
			Thread.sleep(1000 + 1500); //wait index was done
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt(); //si interrupt on relance
		}
	}

}
