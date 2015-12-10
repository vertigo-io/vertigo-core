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
package io.vertigo.struts2.plugins.context.berkeley;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.struts2.core.KActionContext;
import io.vertigo.struts2.impl.context.ContextCachePlugin;

import java.io.File;

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
 * Implémentation Berkeley du ContextCachePlugin.
 * La purge est assurée par un Timer et passe toutes les Math.min(15 min, timeToLiveSeconds).
 *
 * @author pchretien, npiedeloup
 */
public final class BerkeleyContextCachePlugin implements Activeable, ContextCachePlugin {

	private static final String USER_HOME = "user.home";
	private static final String USER_DIR = "user.dir";
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final Logger LOGGER = Logger.getLogger(BerkeleyContextCachePlugin.class);
	private final TupleBinding<CacheValue> cacheValueBinding;
	private final TupleBinding<String> keyBinding = TupleBinding.getPrimitiveBinding(String.class);

	private final long timeToLiveSeconds;
	private Database cacheDatas;

	private final File myCacheEnvPath;
	private Environment myEnv;

	/**
	 * Constructeur.
	 * @param codecManager Manager des mécanismes de codage/décodage.
	 * @param daemonManager Manager des daemons
	 * @param cachePath Chemin de stockage
	 * @param timeToLiveSeconds Durée de vie des éléments en seconde
	 */
	@Inject
	public BerkeleyContextCachePlugin(final CodecManager codecManager, final DaemonManager daemonManager, @Named("cachePath") final String cachePath, @Named("timeToLiveSeconds") final int timeToLiveSeconds) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.timeToLiveSeconds = timeToLiveSeconds;
		final String translatedCachePath = translatePath(cachePath);
		myCacheEnvPath = new File(translatedCachePath);
		myCacheEnvPath.mkdirs();
		Assertion.checkState(myCacheEnvPath.canWrite(), "L'espace de stockage du cache n'est pas accessible ({0})", myCacheEnvPath.getAbsolutePath());

		cacheValueBinding = new CacheValueBinding(new SerializableBinding(codecManager.getCompressedSerializationCodec()));

		final int purgePeriod = Math.min(15 * 60, timeToLiveSeconds);
		daemonManager.registerDaemon("purgeContextCache", RemoveTooOldElementsDaemon.class, purgePeriod, this);
	}

	private static String translatePath(final String path) {
		String translatedPath = path.replaceAll(USER_HOME, System.getProperty(USER_HOME).replace('\\', '/'));
		translatedPath = translatedPath.replaceAll(USER_DIR, System.getProperty(USER_DIR).replace('\\', '/'));
		translatedPath = translatedPath.replaceAll(JAVA_IO_TMPDIR, System.getProperty(JAVA_IO_TMPDIR).replace('\\', '/'));
		return translatedPath;
	}

	/** {@inheritDoc} */
	@Override
	public void put(final KActionContext context) {
		Assertion.checkNotNull(context);
		//-----
		//totalPuts++;
		try {
			final Transaction transaction = createTransaction();
			boolean committed = false;
			try {
				final DatabaseEntry theKey = new DatabaseEntry();
				keyBinding.objectToEntry(context.getId(), theKey);
				final DatabaseEntry theData = new DatabaseEntry();
				cacheValueBinding.objectToEntry(new CacheValue(context), theData);

				final OperationStatus status = cacheDatas.put(transaction, theKey, theData);
				if (!OperationStatus.SUCCESS.equals(status)) {
					throw new MsgDatabaseException("la sauvegarde a échouée");
				}
				transaction.commit();
				committed = true;
			} finally {
				if (!committed) {
					transaction.abort();
				}
			}
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public KActionContext get(final String key) {
		//totalCalls++;
		try {
			final DatabaseEntry theKey = new DatabaseEntry();
			keyBinding.objectToEntry(key, theKey);
			final DatabaseEntry theData = new DatabaseEntry();
			final OperationStatus status = cacheDatas.get(null, theKey, theData, null);
			if (OperationStatus.SUCCESS.equals(status)) {
				final CacheValue cacheValue = readCacheValueSafely(theKey, theData);
				if (cacheValue != null && !isTooOld(cacheValue)) { //null si erreur de lecture
					//totalHits++;
					return (KActionContext) cacheValue.getValue();
				}
				cacheDatas.delete(null, theKey);
			}
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
		return null;
	}

	private CacheValue readCacheValueSafely(final DatabaseEntry theKey, final DatabaseEntry theData) {
		String key = "IdError";
		try {
			key = keyBinding.entryToObject(theKey);
			final CacheValue cacheValue = cacheValueBinding.entryToObject(theData);
			return cacheValue;
		} catch (final RuntimeException e) {
			LOGGER.warn("Erreur de lecture du ContextCache : suppression de l'entrée incrimin�e : " + key, e);
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
			//dés qu'on en trouve un trop récent, on stop
			while (checked < maxChecked && cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				final CacheValue cacheValue = readCacheValueSafely(foundKey, foundData);
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
	public void start() {
		try {
			myEnv = createDbEnv();
			cacheDatas = createDb();
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		if (myEnv != null) {
			try {
				cacheDatas.close();
				// Finally, close the environment.
				myEnv.close();
			} catch (final DatabaseException dbe) {
				LOGGER.error("Error closing BerkeleyContextCachePlugin: " + dbe.toString(), dbe);
			}
		}
	}

	private Environment createDbEnv() throws DatabaseException {
		final EnvironmentConfig myEnvConfig = new EnvironmentConfig();
		myEnvConfig.setReadOnly(false);
		myEnvConfig.setAllowCreate(true);
		myEnvConfig.setTransactional(true);
		//On limite l'utilisation du cache à 20% de la mémoire globale.
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
			db = myEnv.openDatabase(null, "KActionContext", myDbConfig);
		} catch (final DatabaseException e) {
			throw new WrappedException(e);
		}
		return db;
	}

	private static final class MsgDatabaseException extends DatabaseException {
		private static final long serialVersionUID = -2201117970033615366L;

		MsgDatabaseException(final String message) {
			super(message);
		}
	}

	/**
	 * @author npiedeloup
	 */
	static final class RemoveTooOldElementsDaemon implements Daemon {
		private static final Logger TIMER_LOGGER = Logger.getLogger(RemoveTooOldElementsDaemon.class);

		private final BerkeleyContextCachePlugin berkeleyContextCachePlugin;

		/**
		 * @param berkeleyContextCachePlugin This plugin
		 */
		public RemoveTooOldElementsDaemon(final BerkeleyContextCachePlugin berkeleyContextCachePlugin) {
			Assertion.checkNotNull(berkeleyContextCachePlugin);
			//------
			this.berkeleyContextCachePlugin = berkeleyContextCachePlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {
				berkeleyContextCachePlugin.removeTooOldElements();
			} catch (final DatabaseException dbe) {
				TIMER_LOGGER.error("Error closing BerkeleyContextCachePlugin: " + dbe.toString(), dbe);
			}
		}
	}
}
