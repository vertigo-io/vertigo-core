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
package io.vertigo.dynamo.plugins.kvdatabase.berkeley;

import io.vertigo.dynamo.impl.store.kvstore.KVDataStorePlugin;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Implémentation d'un store BerkeleyDB.
 *
 * @author  pchretien
 */
public final class BerkeleyKVDataStorePlugin implements KVDataStorePlugin, Activeable {
	private static final boolean READONLY = false;

	//	private final VTransactionResourceId<LuceneResource> luceneResourceId = new VTransactionResourceId<LuceneResource>(VTransactionResourceId.Priority.NORMAL, "demo-lucene");
	//	private final Directory directory;
	private final String dataStoreName;
	private final VTransactionManager transactionManager;
	private final File dbFile;
	private final boolean inMemory;

	private Environment environment;
	private Database database;
	private BerkeleyDatabase berkeleyDatabase;

	/**
	 * Constructeur.
	 * @param dbFileName Base Berkeley DB
	 * @param transactionManager Manager des transactions
	 */
	@Inject
	public BerkeleyKVDataStorePlugin(final @Named("dataStoreName") String dataStoreName, @Named("fileName") final String dbFileName /*, final LuceneDB luceneDb*/, @Named("inMemory") final boolean inMemory, final VTransactionManager transactionManager) {
		Assertion.checkArgNotEmpty(dataStoreName);
		Assertion.checkArgNotEmpty(dbFileName);
		Assertion.checkNotNull(transactionManager);
		//-----
		this.dataStoreName = dataStoreName;
		dbFile = new File(dbFileName);
		this.transactionManager = transactionManager;
		this.inMemory = inMemory;
	}

	/** {@inheritDoc} */
	@Override
	public String getDataStoreName() {
		return dataStoreName;
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

		database = environment.openDatabase(null, "MyDB", databaseConfig);

		berkeleyDatabase = new BerkeleyDatabase(database, transactionManager);
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		try {
			if (database != null) {
				database.close();
			}
		} finally {
			if (environment != null) {
				environment.close();
			}
		}
	}

	/*private LuceneResource obtainLuceneResource() {
		final VTransaction transaction = getTransactionManager().getCurrentTransaction();
		final LuceneResource ktr = transaction.getResource(luceneResourceId);
		if (ktr == null) {
			//On a rien trouvé il faut créer la resourceLucene et l'ajouter à la transaction
			ktr = new LuceneResource(directory);
			transaction.addResource(luceneResourceId, ktr);
		}
		return ktr;
	}
	//
	//	/** {@inheritDoc} */
	//	public <D extends DtObject> D load(final ) {
	//		//System.out.println(">>>load");
	//		final Long id = getId(uri);
	//		//System.out.println("load>>" + id);
	//		return obtainBerkeleyResource().obtainBerkeleyReader().<D> loadDtObject(id);
	//	}
	//
	//	//	/** {@inheritDoc} */
	//	//	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Integer maxRows) throws KSystemException {
	//	//		//        buildIndex(definition);
	//	//		return obtainBerkeleyResource().obtainBerkeleyReader().<D> loadDtList(dtDefinition, maxRows);
	//	//	}
	//
	//	/** {@inheritDoc} */
	//	public void put(final DtObject dto) {
	//		//System.out.println(">>>put");
	//		if (getId(dto) == null) {
	//			obtainBerkeleyResource().obtainBerkeleyWriter().insert(dto);
	//			//	obtainLuceneResource().obtainLuceneWriter().insert(dto);
	//		} else {
	//			obtainBerkeleyResource().obtainBerkeleyWriter().update(dto);
	//			//		obtainLuceneResource().obtainLuceneWriter().update(dto);
	//		}
	//	}
	//
	//	/** {@inheritDoc} */
	//	public void delete(final URI<? extends DtObject> uri) throws KRuntimeException {
	//		//System.out.println(">>>remove");
	//		obtainBerkeleyResource().obtainBerkeleyWriter().delete(uri);
	//		//	obtainLuceneResource().obtainLuceneWriter().delete(uri);
	//	}
	//
	//	/*
	//	 * NULLABLE
	//	 * Récupération de la clé d'un objet.
	//	 */
	//	private static Long getId(final DtObject dto) {
	//		final DtField pkField = DtObjectUtil.getPrimaryKey(DtObjectUtil.findDtDefinition(dto));
	//		return (Long) pkField.getDataAccessor().getValue(dto);
	//	}
	//
	//	private static Long getId(final URI<? extends DtObject> uri) {
	//		return (Long) uri.getKey();
	//	}
	//
	//	//==========================================================================
	//	//=====================Gestion des associations=============================
	//	//==========================================================================
	//	/** {@inheritDoc} */
	//	public <D extends DtObject> DtList<D> loadList(final DtListURI dtcURI/*, final Integer maxRows*/) {
	//		//System.out.println(">>>loadList2");
	//
	//		//final AssociationDefinition associationDefinition = dtcURI.getAssociationDefinition();
	//		//final DtObjectURI from = dtcURI.getSource();
	//
	//		//	final List<String> list = obtainLuceneResource().obtainLuceneReader().getURINameListForAssociation((DtListURIForAssociation) dtcURI);
	//		final DtList<D> dtc = new DtList<D>(dtcURI.getDtDefinition());
	//		//		for (final String urn : list) {
	//		//			/*	if (maxRows != null && dtc.size() >= maxRows) {
	//		//					break;
	//		//				}*/
	//		//			final DtObjectURI uri = URIHelper.readURN(urn);
	//		//			final D item = getStoreeManager().getDataStore().<D> get(uri);
	//-----
	//		//			dtc.add(item);
	//		//		}
	//		//==================================================================
	//		return dtc;
	//	}
	//
	//	/** {@inheritDoc} */
	//	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
	//		//System.out.println(">>>loadList");
	//		Assertion.notNull(dtDefinition);
	//		Assertion.notNull(criteria instanceof SimpleCriteria, "Ce store ne gère que des SimpleCriteria.");
	//-----
	//		return obtainBerkeleyResource().obtainBerkeleyReader().loadDtList(dtDefinition, maxRows);
	//		//final List<String> list = obtainLuceneResource().obtainLuceneReader().getURINameList(dtDefinition, ((SimpleCriteria) criteria).getSearch(), maxRows);
	//		//final DtList<D> dtc = new DtList<D>(dtDefinition);
	//		//		for (final String urn : list) {
	//		//			final DtObjectURI uri = URIHelper.readURN(urn);
	//		//			try {
	//		//				final D item = getStoreManager().getDataStore().<D> get(uri);
	//-----
	//		//				dtc.add(item);
	//		//			} catch (final Exception e) {
	//		//			}
	//		//		}
	//		//return dtc;
	//	}

	@Override
	public void remove(final String id) {
		berkeleyDatabase.delete(id);
	}

	@Override
	public void put(final String id, final Object object) {
		berkeleyDatabase.put(id, object);
	}

	@Override
	public <C> Option<C> find(final String id, final Class<C> clazz) {
		return berkeleyDatabase.find(id, clazz);
	}

	@Override
	public <C> List<C> findAll(final int skip, final Integer limit, final Class<C> clazz) {
		return berkeleyDatabase.findAll(skip, limit, clazz);
	}
}

//==========================================================================
//==========================INDEXATION======================================
//==========================================================================

/*   private static class InnerVisitor implements Visitor<DtObject> {
       private final LuceneWriter luceneWriter;
       private final IndexWriter writer;

       InnerVisitor(LuceneWriter luceneWriter, IndexWriter writer) {
           this.luceneWriter = luceneWriter;
           this.writer = writer;
       }

       public void visit(DtObject dto) throws Exception {
           //writer.addDocument(luceneWriter.createDocument(dto));
       }
   }

 */
/*    public final void buildIndex(DtDefinition dtDefinition) throws Exception {
        IndexWriter writer = null;
        try {
            writer = luceneWriter.createWriter();

            Visitor v = new InnerVisitor(luceneWriter, writer);
            Sy  stem.out.println("buildIndex begin");
            try {
                berkeleyReader.visitAll(null, v);
            } catch (Exception e) {
                throw new KSystemException("BDD erreur en création Index", e);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
 */
