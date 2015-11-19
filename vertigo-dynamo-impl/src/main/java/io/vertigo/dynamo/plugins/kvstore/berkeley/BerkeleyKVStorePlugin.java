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
package io.vertigo.dynamo.plugins.kvstore.berkeley;

import io.vertigo.dynamo.impl.kvstore.KVStorePlugin;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.ListBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Implémentation d'un store BerkeleyDB.
 *
 * @author  pchretien
 */
public final class BerkeleyKVStorePlugin implements KVStorePlugin, Activeable {
	private static final boolean READONLY = false;

	private final List<String> collections;

	private final VTransactionManager transactionManager;
	private final File dbFile;
	private final boolean inMemory;

	private Environment environment;
	private final Map<String, BerkeleyDatabase> databases = new HashMap<>();

	/**
	 * Constructor.
	 * @param collections List of collections managed by this plugin (comma separated)
	 * @param dbFileName Base Berkeley DB
	 * @param inMemory If store in memory
	 * @param transactionManager Manager des transactions
	 */
	@Inject
	public BerkeleyKVStorePlugin(
			final @Named("collections") String collections,
			@Named("fileName") final String dbFileName,
			@Named("inMemory") final boolean inMemory,
			final VTransactionManager transactionManager) {
		Assertion.checkArgNotEmpty(collections);
		Assertion.checkArgNotEmpty(dbFileName);
		Assertion.checkNotNull(transactionManager);
		//-----
		final ListBuilder<String> listBuilder = new ListBuilder<>();
		for (final String collection : collections.split(", ")) {
			listBuilder.add(collection.trim());
		}
		this.collections = listBuilder.unmodifiable().build();
		//-----
		dbFile = new File(dbFileName);
		this.transactionManager = transactionManager;
		this.inMemory = inMemory;
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getCollections() {
		return collections;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final boolean readOnly = READONLY;
		final EnvironmentConfig environmentConfig = new EnvironmentConfig()
				.setConfigParam(EnvironmentConfig.LOG_MEM_ONLY, inMemory ? "true" : "false")
				.setReadOnly(readOnly)
				.setAllowCreate(!readOnly)
				.setTransactional(!readOnly);

		environment = new Environment(dbFile, environmentConfig);

		final DatabaseConfig databaseConfig = new DatabaseConfig()
				.setReadOnly(readOnly)
				.setAllowCreate(!readOnly)
				.setTransactional(!readOnly);

		for (final String collection : collections) {
			databases.put(collection, new BerkeleyDatabase(environment.openDatabase(null, collection, databaseConfig), transactionManager));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		try {
			for (final BerkeleyDatabase berkeleyDatabase : databases.values()) {
				berkeleyDatabase.getDatabase().close();
			}
		} finally {
			if (environment != null) {
				environment.close();
			}
		}
	}

	private BerkeleyDatabase getDatabase(final String collection) {
		final BerkeleyDatabase database = databases.get(collection);
		Assertion.checkNotNull("database {0] not null", collection);
		return database;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String id) {
		getDatabase(collection).delete(id);
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String id, final Object object) {
		getDatabase(collection).put(id, object);
	}

	/** {@inheritDoc} */
	@Override
	public <C> Option<C> find(final String collection, final String id, final Class<C> clazz) {
		return getDatabase(collection).find(id, clazz);
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final String collection, final int skip, final Integer limit, final Class<C> clazz) {
		return getDatabase(collection).findAll(skip, limit, clazz);
	}

}
