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

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.transaction.KTransaction;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionResourceId;

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
	 * @param key id de l'objet à récupérer
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
		//System.out.println(">>>doLoadDtList......");

		try {
			final Cursor cursor = database.openCursor(getCurrentBerkeleyTransaction(), null);
			try {
				int find = 0;
				while ((limit == null || find < limit + skip) && cursor.getNext(idEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
					final Object object = dataBinding.entryToObject(dataEntry);
					//@todo Pour l'instant on ne comptabilise que les collections du type demandé.
					//System.out.println(">>>sto>" + dto);
					if (clazz.isInstance(object)) {
						find++;
						if (find > skip) {
							list.add(clazz.cast(object));
						}
					}
				}
				return list;
			} finally {
				cursor.close();
			}
		} catch (final DatabaseException e) {
			throw new RuntimeException("findAll a échouée");
		}
	}

	//	/**
	//	 * Ajout d'un nouvel objet.
	//	 * @param dto DTO à ajouter 
	//	 */
	//	void insert(final DtObject dto) {
	//		Assertion.notNull(dto);
	//		//======================================================================
	//		try {
	//			doInsert(dto);
	//			//System.out.println(">>>doInsert ok");
	//		} catch (final DatabaseException e) {
	//			throw new KRuntimeException(e);
	//		}
	//	}

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

	//
	//	/**
	//	 * Modification d'un objet. 
	//	 * @param dto DTO à modifier
	//	 */
	//	void update(final DtObject dto) {
	//		Assertion.notNull(dto);
	//		//======================================================================
	//		try {
	//			doUpdate(dto);
	//		} catch (final DatabaseException e) {
	//			throw new KRuntimeException(e);
	//		}
	//	}
	//
	//	private void doInsert(final DtObject dto) throws DatabaseException {
	//		Assertion.isNull(DtObjectUtil.getId(dto), "insertion impossible");
	//		//======================================================================
	//		//Création d'un objet
	//		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);
	//		final long key = createSequence(dtDefinition);
	//		final DtField pkField = DtObjectUtil.getPrimaryKey(dtDefinition);
	//		pkField.getDataAccessor().setValue(dto, key);
	//		//======================================================================
	//		saveDtObject(dto, key);
	//	}
	//
	//	private void doUpdate(final DtObject dto) throws DatabaseException {
	//		final Object id = DtObjectUtil.getId(dto);
	//		Assertion.notNull(id, "maj impossible");
	//		saveDtObject(dto, (Long) id);
	//	}
	//
	//	private void doDelete(final URI<? extends DtObject> uri) throws DatabaseException {
	//		idBinding.objectToEntry(uri.getKey(), theKey);
	//		berkeleyResource.getDatabase().delete(berkeleyResource.obtainTransaction(), theKey);
	//	}
	//
	//	private void saveDtObject(final DtObject dto, final long id) throws DatabaseException {
	//		idBinding.objectToEntry(id, theKey);
	//		dtoBinding.objectToEntry(dto, theData);
	//		final OperationStatus status = berkeleyResource.getDatabase().put(berkeleyResource.obtainTransaction(), theKey, theData);
	//		if (!OperationStatus.SUCCESS.equals(status)) {
	//			throw new DatabaseException("la sauvegarde a échouée");
	//		}
	//	}
	//
	//	private final int cacheSize = 10000;
	//
	//	private final List<Long> cache = new ArrayList<Long>(cacheSize);
	//
	//	private synchronized long createSequence(final DtDefinition dtDefinition) throws DatabaseException {
	//		if (cache.size() == 0) {
	//			final String sequenceName = dtDefinition.getName();
	//			final DatabaseEntry theSequence = new DatabaseEntry();
	//			TupleBinding.getPrimitiveBinding(String.class).objectToEntry(sequenceName, theSequence);
	//			SequenceConfig.DEFAULT.setAllowCreate(true);
	//			final Sequence sequence = berkeleyResource.getSequenceDB().openSequence(null, theSequence, SequenceConfig.DEFAULT);
	//			try {
	//				final long nextSequence = sequence.get(null, cacheSize);
	//				for (long i = 0; i < cacheSize; i++) {
	//					cache.add(nextSequence + i);
	//				}
	//			} finally {
	//				sequence.close();
	//			}
	//		}
	//		return cache.remove(0);
	//	}

}
//	/**
//	 * Récupération d'une liste d'objets . 
//	 * @param <C> D Type des objets à récupérer
//	 * @param max Nombre maximal d'objets à récupérer, si null tous les objets sont récupérés
//	 * @return DTC 
//	 */
//	<D extends DtObject> DtList<D> doFind(final DtDefinition dtDefinition, final Integer max) {
//		Assertion.notNull(dtDefinition);
//		//Assertion.notNull(max);
//		//---------------------------------------------------------------------
//		try {
//			return this.<D> doLoadDtList(dtDefinition, max);
//		} catch (final DatabaseException e) {
//			throw new KRuntimeException(e);
//		}
//	}

//
/**
 * Permet de visiter tous les éléments afin de reconstruir un index.
 * On ne construit pas de collection, on peut ainsi parcourir des listes très importantes.
 * @param visitor Visitor
 * @throws DatabaseException
 */
/*   public void visitAll(Transaction txn, Visitor visitor) throws Exception {
       Cursor cursor = berkeleyResource.getDB().openCursor(txn, null);

       // DatabaseEntry objects used for reading records
       DatabaseEntry foundKey = new DatabaseEntry();
       DatabaseEntry foundData = new DatabaseEntry();

       long start = System.currentTimeMillis();
       try {
           for (int i = 0; (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS); i++) {
               DTO dto = (DTO)dtoBinding.entryToObject(foundData);
               visitor.visit(dto);
               if ((i % 1000) == 0) {
                   //System.out.println(" >>" + i + "; time=" + (System.currentTimeMillis() - start));
               }
           }
       } finally {
           if (cursor != null) {
               cursor.close();
           }
       }
   }*/

