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
package io.vertigo.dynamo.plugins.kvdatastore.berkeley;

import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionResourceId;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

/**
 * Objet d'accès en lecture/écriture à la base Berkeley.
 * @author pchretien
 */
final class BerkeleyDatabase {
	private final KTransactionResourceId<BerkeleyResource> berkeleyResourceId = new KTransactionResourceId<>(KTransactionResourceId.Priority.TOP, "demo-berkeley");
	private static final TupleBinding dataBinding = new BerkeleyDataBinding();
	private static final EntryBinding keyBinding = TupleBinding.getPrimitiveBinding(String.class);
	private final KTransactionManager transactionManager;
	private final Database database;

	/**
	 * Constructor.
	 * @param database Berkeley DataBase
	 * @param transactionManager Transaction manager
	 */
	BerkeleyDatabase(final Database database, final KTransactionManager transactionManager) {
		Assertion.checkNotNull(database);
		Assertion.checkNotNull(transactionManager);
		//---------------------------------------------------------------------
		this.transactionManager = transactionManager;
		this.database = database;
	}

	private Transaction getCurrentBerkeleyTransaction() {
		final KTransaction transaction = transactionManager.getCurrentTransaction();
		BerkeleyResource berkeleyResource = transaction.getResource(berkeleyResourceId);
		if (berkeleyResource == null) {
			//On a rien trouvé il faut créer la resourceLucene et l'ajouter à la transaction
			berkeleyResource = new BerkeleyResource(database);
			transaction.addResource(berkeleyResourceId, berkeleyResource);
		}
		return berkeleyResource.getBerkeleyTransaction();
	}

	/**
	 * Récupération d'un Objet par sa clé.
	 * @param <C> D Type des objets à récupérer
	 * @param id Id de l'objet à récupérer
	 * @return Objet correspondant à la clé
	 */
	<C> Option<C> find(final String id, final Class<C> clazz) {
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------
		final DatabaseEntry idEntry = new DatabaseEntry();
		final DatabaseEntry dataEntry = new DatabaseEntry();

		keyBinding.objectToEntry(id, idEntry);

		final OperationStatus status;
		try {
			status = database.get(getCurrentBerkeleyTransaction(), idEntry, dataEntry, LockMode.DEFAULT);
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
		if (status == OperationStatus.NOTFOUND) {
			//Si on n'a rien trouvé
			return Option.none();
		}
		if (!OperationStatus.SUCCESS.equals(status)) {
			throw new RuntimeException("find a échouée");
		}
		return Option.some(clazz.cast(dataBinding.entryToObject(dataEntry)));
	}

	void put(final String id, final Object object) {
		Assertion.checkArgNotEmpty(id);
		Assertion.checkNotNull(object);
		//---------------------------------------------------------------------
		final DatabaseEntry idEntry = new DatabaseEntry();
		final DatabaseEntry dataEntry = new DatabaseEntry();

		keyBinding.objectToEntry(id, idEntry);
		dataBinding.objectToEntry(object, dataEntry);

		final OperationStatus status;
		try {
			status = database.put(getCurrentBerkeleyTransaction(), idEntry, dataEntry);
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
		if (!OperationStatus.SUCCESS.equals(status)) {
			throw new RuntimeException("put a échouée");
		}
	}

	public <C> List<C> findAll(final int skip, final Integer limit, final Class<C> clazz) {
		final DatabaseEntry idEntry = new DatabaseEntry();
		final DatabaseEntry dataEntry = new DatabaseEntry();
		final List<C> list = new ArrayList<>();

		try (final Cursor cursor = database.openCursor(getCurrentBerkeleyTransaction(), null)) {
			int find = 0;
			while ((limit == null || find < limit + skip) && cursor.getNext(idEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				final Object object = dataBinding.entryToObject(dataEntry);
				//@todo Pour l'instant on ne comptabilise que les collections du type demandé.
				if (clazz.isInstance(object)) {
					find++;
					if (find > skip) {
						list.add(clazz.cast(object));
					}
				}
			}
			return list;
		} catch (final DatabaseException e) {
			throw new RuntimeException("findAll a échouée");
		}
	}

	/**
	 * @param id Element id to remove
	 */
	void delete(final String id) {
		Assertion.checkArgNotEmpty(id);
		//---------------------------------------------------------------------
		final DatabaseEntry idEntry = new DatabaseEntry();

		keyBinding.objectToEntry(id, idEntry);

		final OperationStatus status;
		try {
			status = database.delete(getCurrentBerkeleyTransaction(), idEntry);
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
		if (!OperationStatus.SUCCESS.equals(status)) {
			throw new RuntimeException("delete a échouée");
		}
	}
}
