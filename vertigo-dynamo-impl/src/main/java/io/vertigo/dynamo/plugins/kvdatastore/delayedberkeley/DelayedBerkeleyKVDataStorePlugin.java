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
package io.vertigo.dynamo.plugins.kvdatastore.delayedberkeley;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.kvdatastore.KVDataStorePlugin;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import com.sleepycat.je.Transaction;

/**
 * Implémentation Berkeley du KVDataStorePlugin.
 * La purge est assurée par un Timer et passe toutes les Math.min(5 min, timeToLiveSeconds).
 *
 * @author pchretien, npiedeloup
 */
public final class DelayedBerkeleyKVDataStorePlugin implements KVDataStorePlugin, Activeable {
	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private final Logger logger = Logger.getLogger(getClass());
	private final TupleBinding<CacheValue> cacheValueBinding;
	private final TupleBinding<String> keyBinding = TupleBinding.getPrimitiveBinding(String.class);

	private final String storeName;
	private Timer purgeTimer;
	private final long timeToLiveSeconds;
	private Database cacheDatas;

	private final File myCacheEnvPath;
	private Environment myEnv;

	/**
	 * Constructeur.
	 * @param codecManager Manager des mécanismes de codage/décodage. 
	 * @param cachePath Chemin de stockage
	 * @param timeToLiveSeconds Durée de vie des éléments en seconde
	 */
	@Inject
	public DelayedBerkeleyKVDataStorePlugin(final CodecManager codecManager, final @Named("dataStoreName") String storeName, final @Named("cachePath") String cachePath, final @Named("timeToLiveSeconds") int timeToLiveSeconds) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkArgNotEmpty(storeName);
		//---------------------------------------------------------------------
		this.storeName = storeName;
		this.timeToLiveSeconds = timeToLiveSeconds;
		final String translatedCachePath = translatePath(cachePath);
		myCacheEnvPath = new File(translatedCachePath);
		myCacheEnvPath.mkdirs();
		Assertion.checkState(myCacheEnvPath.canWrite(), "Can't access cache storage directory ({0})", myCacheEnvPath.getAbsolutePath());

		cacheValueBinding = new CacheValueBinding(new SerializableBinding(codecManager.getCompressedSerializationCodec()));
	}

	private static String translatePath(final String path) {
		return path//
				.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'))//
				.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'))//
				.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
	}

	/** {@inheritDoc} */
	@Override
	public String getDataStoreName() {
		return storeName;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final String key, final Object data) {
		Assertion.checkNotNull(data);
		Assertion.checkArgument(data instanceof Serializable, "Value must be Serializable {0}", data.getClass().getSimpleName());
		//---------------------------------------------------------------------
		//totalPuts++;
		try {
			final Transaction transaction = createTransaction();
			boolean committed = false;
			try {
				final DatabaseEntry theKey = new DatabaseEntry();
				keyBinding.objectToEntry(key, theKey);
				final DatabaseEntry theData = new DatabaseEntry();
				cacheValueBinding.objectToEntry(new CacheValue((Serializable) data), theData);

				final OperationStatus status = cacheDatas.put(transaction, theKey, theData);
				if (!OperationStatus.SUCCESS.equals(status)) {
					throw new DatabaseException("Write error in UiSecurityTokenCache") { //DatabaseException est abstract, mais aucun fils ne semble correct
						private static final long serialVersionUID = -2201117970033615366L;
					};
				}
				transaction.commit();
				committed = true;
			} finally {
				if (!committed) {
					transaction.abort();
				}
			}
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <C> Option<C> find(final String key, final Class<C> clazz) {
		//totalCalls++;
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			final DatabaseEntry theData = new DatabaseEntry();
			final OperationStatus status = cacheDatas.get(null, theKey, theData, null);
			if (OperationStatus.SUCCESS.equals(status)) {
				final CacheValue cacheValue = readCacheValueSafely(theKey, theData);
				if (cacheValue == null || isTooOld(cacheValue)) {//null if read error
					cacheDatas.delete(null, theKey); //if corrupt (null) or too old, we delete it					
				} else {
					return Option.some(clazz.cast(cacheValue.getValue()));
				}
			}
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
		return Option.none();
	}

	/** {@inheritDoc} */
	@Override
	public <C> List<C> findAll(final int skip, final Integer limit, final Class<C> clazz) {
		final DatabaseEntry theKey = new DatabaseEntry();
		final DatabaseEntry theData = new DatabaseEntry();
		final List<C> list = new ArrayList<>();
		try {
			final Cursor cursor = cacheDatas.openCursor(null, null);
			try {
				int find = 0;
				while ((limit == null || find < limit + skip) && cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					final CacheValue cacheValue = cacheValueBinding.entryToObject(theData);
					if (cacheValue == null || isTooOld(cacheValue)) {//null if read error
						cursor.delete(); //if corrupt (null) or too old, we delete it
					} else {
						final Serializable value = cacheValue.getValue();
						if (clazz.isInstance(value)) { //we only count asked class objects
							find++;
							if (find > skip) {
								list.add(clazz.cast(value));
							}
						}
						//totalHits++;
					}

				}
				return list;
			} finally {
				cursor.close();
			}
		} catch (final DatabaseException e) {
			throw new RuntimeException("findAll failed");
		}
	}

	/** {@inheritDoc} */
	public void remove(final String key) {
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			cacheDatas.delete(null, theKey);
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	private CacheValue readCacheValueSafely(final DatabaseEntry theKey, final DatabaseEntry theData) {
		String key = "IdError";
		try {
			key = keyBinding.entryToObject(theKey);
			final CacheValue cacheValue = cacheValueBinding.entryToObject(theData);
			return cacheValue;
		} catch (final RuntimeException e) {
			logger.warn("Read error in UiSecurityTokenCache : remove tokenKey : " + key, e);
			cacheDatas.delete(null, theKey);
		}
		return null;
	}

	private boolean isTooOld(final CacheValue cacheValue) {
		return System.currentTimeMillis() - cacheValue.getCreateTime() >= timeToLiveSeconds * 1000;
	}

	private Transaction createTransaction() throws DatabaseException {
		return cacheDatas.getEnvironment().beginTransaction(null, null);
	}

	/**
	 * Purge les elements trop vieux.
	 * @throws DatabaseException Si erreur
	 */
	void removeTooOldElements() throws DatabaseException {
		final DatabaseEntry foundKey = new DatabaseEntry();
		final DatabaseEntry foundData = new DatabaseEntry();
		try (Cursor cursor = cacheDatas.openCursor(null, null)) {
			final int maxChecked = 500;
			int checked = 0;
			//Les elements sont parcouru dans l'ordre d'insertion (sans lock)
			//dès qu'on en trouve un trop récent, on stop
			while (checked < maxChecked && cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				final CacheValue cacheValue = readCacheValueSafely(foundKey, foundData);
				if (cacheValue == null || isTooOld(cacheValue)) {//null si erreur de lecture
					cacheDatas.delete(null, foundKey);
					checked++;
				} else {
					break;
				}
			}
			logger.info("purge " + checked + " elements");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		try {
			myEnv = createDbEnv();
			cacheDatas = createDb();
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}

		final long purgePeriod = Math.min(15 * 60 * 1000, timeToLiveSeconds * 1000);
		purgeTimer = new Timer("PurgeContextCache", true);
		purgeTimer.schedule(new TimerTask() {
			private final Logger timerLogger = Logger.getLogger(getClass());

			@Override
			public void run() {
				try {
					removeTooOldElements();
				} catch (final DatabaseException dbe) {
					timerLogger.error("Error closing BerkeleyContextCachePlugin: " + dbe.toString(), dbe);
				}
			}
		}, purgePeriod, purgePeriod);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		purgeTimer.cancel();
		if (myEnv != null) {
			try {
				cacheDatas.close();
				// Finally, close the environment.
				myEnv.close();
			} catch (final DatabaseException dbe) {
				logger.error("Error closing " + getClass().getSimpleName() + ": " + dbe.toString(), dbe);
			}
		}
	}

	private Environment createDbEnv() throws DatabaseException {
		final EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		myEnvConfig.setReadOnly(false);
		myEnvConfig.setAllowCreate(true);
		myEnvConfig.setTransactional(true);
		//we limit cache usage to 20% of global memory.
		myEnvConfig.setCachePercent(20);
		// Open the environment
		return new Environment(myCacheEnvPath, myEnvConfig);
	}

	private Database createDb() {
		final DatabaseConfig myDbConfig = new DatabaseConfig();
		myDbConfig.setReadOnly(false);
		myDbConfig.setAllowCreate(true);
		myDbConfig.setTransactional(true);
		final Database db;
		try {
			db = myEnv.openDatabase(null, "KVDataStorePlugin", myDbConfig);
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
		return db;
	}

}
