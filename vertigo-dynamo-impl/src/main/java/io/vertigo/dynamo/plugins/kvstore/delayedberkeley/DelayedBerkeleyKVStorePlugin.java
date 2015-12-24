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
package io.vertigo.dynamo.plugins.kvstore.delayedberkeley;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.dynamo.impl.kvstore.KVStorePlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Implémentation Berkeley du KVDataStorePlugin.
 * Ce store N'EST PAS transactionnel !!
 * La purge est assurée par un Timer et passe toutes les Math.min(5 min, timeToLiveSeconds).
 *
 * @author pchretien, npiedeloup
 */
public final class DelayedBerkeleyKVStorePlugin implements KVStorePlugin, Activeable {
	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final Logger LOGGER = Logger.getLogger(DelayedBerkeleyKVStorePlugin.class);
	private final TupleBinding<DelayedBerkeleyCacheValue> cacheValueBinding;
	private final TupleBinding<String> keyBinding = TupleBinding.getPrimitiveBinding(String.class);

	private final List<String> collections;

	private final int timeToLiveSeconds;
	private Database cacheDatas;

	private final File myCacheEnvPath;
	private Environment environment;

	/**
	 * Constructeur.
	 * @param collections List of collections managed by this plugin (comma separated)
	 * @param codecManager Manager des mécanismes de codage/décodage.
	 * @param daemonManager Manager des daemons
	 * @param cachePath Chemin de stockage
	 * @param timeToLiveSeconds Durée de vie des éléments en seconde
	 */
	@Inject
	public DelayedBerkeleyKVStorePlugin(
			final @Named("collections") String collections,
			final CodecManager codecManager,
			final DaemonManager daemonManager,
			@Named("cachePath") final String cachePath,
			@Named("timeToLiveSeconds") final int timeToLiveSeconds) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkArgNotEmpty(collections);
		//-----
		final ListBuilder<String> listBuilder = new ListBuilder<>();
		for (final String collection : collections.split(", ")) {
			listBuilder.add(collection.trim());
		}
		this.collections = listBuilder.unmodifiable().build();
		//-----

		this.timeToLiveSeconds = timeToLiveSeconds;
		final String translatedCachePath = translatePath(cachePath);
		myCacheEnvPath = new File(translatedCachePath);
		if (!myCacheEnvPath.exists()) {
			final boolean createDirs = myCacheEnvPath.mkdirs();
			Assertion.checkState(createDirs, "Can't create dirs for cache storage directory ({0})", myCacheEnvPath.getAbsolutePath());
		}
		Assertion.checkState(myCacheEnvPath.canWrite(), "Can't access cache storage directory ({0})", myCacheEnvPath.getAbsolutePath());

		cacheValueBinding = new DelayedBerkeleyCacheValueBinding(new DelayedBerkeleySerializableBinding(codecManager.getCompressedSerializationCodec()));

		final int purgePeriod = Math.min(15 * 60, timeToLiveSeconds);
		daemonManager.registerDaemon("purgeContextCache", RemoveTooOldElementsDaemon.class, purgePeriod, this);
	}

	private static String translatePath(final String path) {
		return path
				.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'))
				.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'))
				.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getCollections() {
		return collections;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String collection, final String key, final Object element) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(element);
		Assertion.checkArgument(element instanceof Serializable, "Value must be Serializable {0}", element.getClass().getSimpleName());
		//-----
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			final DatabaseEntry theData = new DatabaseEntry();
			cacheValueBinding.objectToEntry(new DelayedBerkeleyCacheValue((Serializable) element, System.currentTimeMillis()), theData);

			final OperationStatus status = cacheDatas.put(null, theKey, theData);
			if (!OperationStatus.SUCCESS.equals(status)) {
				throw new SimpleDatabaseException("Write error in UiSecurityTokenCache");
			}
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <C> Option<C> find(final String collection, final String key, final Class<C> clazz) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		Assertion.checkNotNull(clazz);
		//-----
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			final DatabaseEntry theData = new DatabaseEntry();
			final OperationStatus status = cacheDatas.get(null, theKey, theData, null);
			if (OperationStatus.SUCCESS.equals(status)) {
				final DelayedBerkeleyCacheValue cacheValue = readCacheValueSafely(theKey, theData);
				if (cacheValue == null || isTooOld(cacheValue)) {//null if read error
					cacheDatas.delete(null, theKey); //if corrupt (null) or too old, we delete it
				} else {
					return Option.some(clazz.cast(cacheValue.getValue()));
				}
			}
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
		return Option.none();
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final String collection, final int skip, final Integer limit, final Class<C> clazz) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkNotNull(clazz);
		//-----
		final DatabaseEntry theKey = new DatabaseEntry();
		final DatabaseEntry theData = new DatabaseEntry();
		final List<C> list = new ArrayList<>();
		try (final Cursor cursor = cacheDatas.openCursor(null, null)) {
			int find = 0;
			while ((limit == null || find < limit + skip) && cursor.getNext(theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				final DelayedBerkeleyCacheValue cacheValue = cacheValueBinding.entryToObject(theData);
				if (cacheValue == null || isTooOld(cacheValue)) {//null if read error
					cacheDatas.delete(null, theKey); //if corrupt (null) or too old, we delete it
				} else {
					final Serializable value = cacheValue.getValue();
					if (clazz.isInstance(value)) { //we only count asked class objects
						find++;
						if (find > skip) {
							list.add(clazz.cast(value));
						}
					}
				}
			}
			return list;
		} catch (final DatabaseException e) {
			throw new WrappedException("findAll failed", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final String collection, final String key) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkArgNotEmpty(key);
		//-----
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			cacheDatas.delete(null, theKey);
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	private DelayedBerkeleyCacheValue readCacheValueSafely(final DatabaseEntry theKey, final DatabaseEntry theData) {
		String key = "IdError";
		try {
			key = keyBinding.entryToObject(theKey);
			return cacheValueBinding.entryToObject(theData);
		} catch (final RuntimeException e) {
			LOGGER.warn("Read error in UiSecurityTokenCache : remove tokenKey : " + key, e);
			cacheDatas.delete(null, theKey);
		}
		return null;
	}

	private boolean isTooOld(final DelayedBerkeleyCacheValue cacheValue) {
		return System.currentTimeMillis() - cacheValue.getCreateTime() >= timeToLiveSeconds * 1000;
	}

	/**
	 * Purge les elements trop vieux.
	 * @throws DatabaseException Si erreur
	 */
	void removeTooOldElements() {
		final DatabaseEntry foundKey = new DatabaseEntry();
		final DatabaseEntry foundData = new DatabaseEntry();
		try (Cursor cursor = cacheDatas.openCursor(null, null)) {
			final int maxChecked = 500;
			int checked = 0;
			//Les elements sont parcouru dans l'ordre d'insertion (sans lock)
			//dès qu'on en trouve un trop récent, on stop
			while (checked < maxChecked && cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				final DelayedBerkeleyCacheValue cacheValue = readCacheValueSafely(foundKey, foundData);
				if (cacheValue == null || isTooOld(cacheValue)) {//null si erreur de lecture
					cacheDatas.delete(null, foundKey);
					checked++;
				} else {
					break;
				}
			}
			LOGGER.info("purge " + checked + " elements");
		}
	}

	/** {@inheritDoc} */
	@Override
	public long count(final String collection) {
		return cacheDatas.count();
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		try {
			environment = createDbEnv();
			cacheDatas = createDb();
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		if (environment != null) {
			try {
				cacheDatas.close();
				// Finally, close the environment.
				environment.close();
			} catch (final DatabaseException dbe) {
				LOGGER.error("Error closing " + getClass().getSimpleName() + ": " + dbe, dbe);
			}
		}
	}

	private Environment createDbEnv() {
		final EnvironmentConfig myEnvConfig = new EnvironmentConfig()
				.setReadOnly(false)
				.setAllowCreate(true)
				.setTransactional(false);
		//we limit cache usage to 20% of global memory.
		myEnvConfig.setCachePercent(20);
		// Open the environment
		return new Environment(myCacheEnvPath, myEnvConfig);
	}

	private Database createDb() {
		final DatabaseConfig myDbConfig = new DatabaseConfig()
				.setReadOnly(false)
				.setAllowCreate(true)
				.setTransactional(false);
		try {
			return environment.openDatabase(null, "KVDataStorePlugin", myDbConfig);
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	//DatabaseException est abstract, mais aucun fils ne semble correct
	/**
	 *
	 * @author npiedeloup
	 */
	static final class SimpleDatabaseException extends DatabaseException {

		private static final long serialVersionUID = -2201117970033615366L;

		/**
		 * Constructor.
		 * @param message Message
		 */
		SimpleDatabaseException(final String message) {
			super(message);
		}
	}

	/**
	 *
	 * @author npiedeloup
	 */
	//must be public to be used by DaemonManager
	public static final class RemoveTooOldElementsDaemon implements Daemon {
		private final DelayedBerkeleyKVStorePlugin delayedBerkeleyKVDataStorePlugin;

		/**
		 * @param delayedBerkeleyKVDataStorePlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final DelayedBerkeleyKVStorePlugin delayedBerkeleyKVDataStorePlugin) {
			Assertion.checkNotNull(delayedBerkeleyKVDataStorePlugin);
			//------
			this.delayedBerkeleyKVDataStorePlugin = delayedBerkeleyKVDataStorePlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				delayedBerkeleyKVDataStorePlugin.removeTooOldElements();
			} catch (final DatabaseException dbe) {
				LOGGER.error("Error closing BerkeleyContextCachePlugin: " + dbe, dbe);
			}
		}
	}
}
