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

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
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

import org.apache.log4j.Logger;

import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Implémentation d'un store BerkeleyDB.
 *
 * @author  pchretien, npiedeloup
 */
public final class BerkeleyKVStorePlugin implements KVStorePlugin, Activeable {
	private static final boolean READONLY = false;
	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private final List<BerkeleyCollectionConfig> collectionConfigs;
	private final List<String> collectionNames;

	private final CodecManager codecManager;
	private final DaemonManager daemonManager;
	private final VTransactionManager transactionManager;
	final String dbFilePathTranslated;

	private Environment fsEnvironment;
	private Environment ramEnvironment;
	private final Map<String, BerkeleyDatabase> databases = new HashMap<>();

	/**
	 * Constructor.
	 * Collections syntax :
	 *  - collections are comma separated
	 *  
	 *  a revoir (param étendus
	 *  - collections may defined TimeToLive and Memory configs with a json like syntax : collName;TTL=10;inMemory
	 *  - TTL default to -1 meaning eternal
	 *  - inMemory default to false meaning store on file system
	 * @param collections List of collections managed by this plugin (comma separated)
	 * @param dbFilePath Base Berkeley DB file system path (Could use java env param like user.home user.dir or java.io.tmpdir)
	 * @param transactionManager Transaction manager
	 * @param codecManager Codec manager
	 * @param daemonManager Daemon manager
	 */
	@Inject
	public BerkeleyKVStorePlugin(
			final @Named("collections") String collections,
			@Named("dbFilePath") final String dbFilePath,
			final boolean inMemory,
			final VTransactionManager transactionManager,
			final CodecManager codecManager,
			final DaemonManager daemonManager) {
		//Assertion.checkArgNotEmpty(collections);
		Assertion.checkArgNotEmpty(dbFilePath);
		Assertion.checkNotNull(transactionManager);
		//-----
		collectionConfigs = parseCollectionConfigs(collections);
		final ListBuilder<String> collectionNamesBuilder = new ListBuilder<>();
		for (final BerkeleyCollectionConfig collectionConfig : collectionConfigs) {
			collectionNamesBuilder.add(collectionConfig.getCollectionName());
		}
		collectionNames = collectionNamesBuilder.unmodifiable().build();
		//-----
		dbFilePathTranslated = translatePath(dbFilePath);
		this.transactionManager = transactionManager;
		this.codecManager = codecManager;
		this.daemonManager = daemonManager;
	}

	private static String translatePath(final String path) {
		return path
				.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'))
				.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'))
				.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
	}

	private static List<BerkeleyCollectionConfig> parseCollectionConfigs(final String collections) {
		//replace by a Json like parser (without " )
		final ListBuilder<BerkeleyCollectionConfig> listBuilder = new ListBuilder<>();
		for (final String collection : collections.split(",")) {
			String collectionName = null;
			long timeToLiveSeconds = -1;
			boolean inMemory = false;
			for (final String collectionDetail : collection.split(";")) {
				if (collectionDetail.startsWith("TTL=")) {
					Assertion.checkState(timeToLiveSeconds == -1L, "Time to live alread defined on {0}", collection);
					timeToLiveSeconds = Long.parseLong(collectionDetail.substring("TTL=".length()));
				} else if (collectionDetail.startsWith("inMemory")) {
					Assertion.checkState(!inMemory, "inMemory alread defined on {0}", collection);
					inMemory = true;
				} else {
					Assertion.checkState(collectionName == null, "collectionName alread defined on {0}", collection);
					collectionName = collectionDetail;
				}
			}
			listBuilder.add(new BerkeleyCollectionConfig(collectionName, timeToLiveSeconds, inMemory));
		}
		return listBuilder.unmodifiable().build();
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getCollections() {
		return collectionNames;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		final boolean readOnly = READONLY;
		ramEnvironment = buildRamEnvironment(new File(dbFilePathTranslated + File.separator + "ram"), readOnly);
		fsEnvironment = buildFsEnvironment(new File(dbFilePathTranslated), readOnly);

		final DatabaseConfig databaseConfig = new DatabaseConfig()
				.setReadOnly(readOnly)
				.setAllowCreate(!readOnly)
				.setTransactional(!readOnly);

		for (final BerkeleyCollectionConfig collectionConfig : collectionConfigs) {
			final BerkeleyDatabase berkeleyDatabase = new BerkeleyDatabase(
					(collectionConfig.isInMemory() ? ramEnvironment : fsEnvironment) //select environment (FS or RAM)
							.openDatabase(null, collectionConfig.getCollectionName(), databaseConfig), //open database
					collectionConfig.getTimeToLiveSeconds(), transactionManager, codecManager);
			databases.put(collectionConfig.getCollectionName(), berkeleyDatabase);
		}

		final int purgePeriodSeconds = 15 * 60;
		daemonManager.registerDaemon("purgeBerkeleyKVStore", RemoveTooOldElementsDaemon.class, purgePeriodSeconds, this);
	}

	private static Environment buildFsEnvironment(final File dbFile, final boolean readOnly) {
		final EnvironmentConfig fsEnvironmentConfig = new EnvironmentConfig()
				.setConfigParam(EnvironmentConfig.LOG_MEM_ONLY, "false")
				.setReadOnly(readOnly)
				.setAllowCreate(!readOnly)
				.setTransactional(!readOnly);
		return new Environment(dbFile, fsEnvironmentConfig);
	}

	private static Environment buildRamEnvironment(final File dbFileRam, final boolean readOnly) {
		final EnvironmentConfig ramEnvironmentConfig = new EnvironmentConfig()
				.setConfigParam(EnvironmentConfig.LOG_MEM_ONLY, "true")
				.setReadOnly(readOnly)
				.setAllowCreate(!readOnly)
				.setTransactional(!readOnly);
		return new Environment(dbFileRam, ramEnvironmentConfig);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		try {
			for (final BerkeleyDatabase berkeleyDatabase : databases.values()) {
				berkeleyDatabase.getDatabase().close();
			}
		} finally {
			if (fsEnvironment != null) {
				fsEnvironment.close();
			}
			if (ramEnvironment != null) {
				ramEnvironment.close();
			}
		}
	}

	private BerkeleyDatabase getDatabase(final String collection) {
		final BerkeleyDatabase database = databases.get(collection);
		Assertion.checkNotNull("database {0} not null", collection);
		return database;
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String id) {
		getDatabase(collection).delete(id);
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String collection) {
		getDatabase(collection).clear();
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String id, final Object element) {
		getDatabase(collection).put(id, element);
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

	/** {@inheritDoc} */
	@Override
	public int count(final String collection) {
		return getDatabase(collection).count();
	}

	/**
	 * Remove too old elements.
	 */
	void removeTooOldElements() {
		for (final String collection : collectionNames) {
			getDatabase(collection).removeTooOldElements();
		}
	}

	/**
	 * Daemon to remove too old elements.
	 * @author npiedeloup
	 */
	//must be public to be used by DaemonManager
	public static final class RemoveTooOldElementsDaemon implements Daemon {
		private static final Logger LOGGER = Logger.getLogger(BerkeleyKVStorePlugin.class);

		private final BerkeleyKVStorePlugin berkeleyKVDataStorePlugin;

		/**
		 * @param berkeleyKVDataStorePlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final BerkeleyKVStorePlugin berkeleyKVDataStorePlugin) {
			Assertion.checkNotNull(berkeleyKVDataStorePlugin);
			//------
			this.berkeleyKVDataStorePlugin = berkeleyKVDataStorePlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				berkeleyKVDataStorePlugin.removeTooOldElements();
			} catch (final DatabaseException dbe) {
				LOGGER.error("Error closing BerkeleyContextCachePlugin: " + dbe, dbe);
			}
		}
	}
}
