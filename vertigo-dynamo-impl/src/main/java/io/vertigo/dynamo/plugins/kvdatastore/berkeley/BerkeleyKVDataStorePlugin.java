package io.vertigo.dynamo.plugins.kvdatastore.berkeley;

import io.vertigo.dynamo.impl.kvdatastore.KVDataStorePlugin;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Implémentation d'un store BerkeleyDB.
 * 
 * @author  pchretien
 * @version $Id: BerkeleyKVDataStorePlugin.java,v 1.1 2013/01/02 13:38:30 pchretien Exp $
 */
public final class BerkeleyKVDataStorePlugin implements KVDataStorePlugin, Activeable {
	private static final boolean READONLY = false;

	//	private final KTransactionResourceId<LuceneResource> luceneResourceId = new KTransactionResourceId<LuceneResource>(KTransactionResourceId.Priority.NORMAL, "demo-lucene");
	//	private final Directory directory;
	private final KTransactionManager transactionManager;
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
	public BerkeleyKVDataStorePlugin(@Named("fileName") final String dbFileName /*, final LuceneDB luceneDb*/, @Named("inMemory") boolean inMemory, final KTransactionManager transactionManager) {
		Assertion.checkArgNotEmpty(dbFileName);
		Assertion.checkNotNull(transactionManager);
		//		Assertion.notNull(luceneDb);
		//--------------------------------------------------------------------------
		dbFile = new File(dbFileName);
		this.transactionManager = transactionManager;
		this.inMemory = inMemory;
	}

	/** {@inheritDoc} */
	public void start() {
		try {
			doStart(READONLY);
			berkeleyDatabase = new BerkeleyDatabase(database, transactionManager);
		} catch (final DatabaseException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Ouverture de la DB.
	 * @param readOnly Si DB en consultation seule.	
	 * @throws DatabaseException Si erreur lors de l'ouverture 
	 */
	private void doStart(final boolean readOnly) throws DatabaseException {

		final EnvironmentConfig environmentConfig = new EnvironmentConfig();
		if (inMemory) {
			environmentConfig.setConfigParam(EnvironmentConfig.LOG_MEM_ONLY, "true");
		}
		environmentConfig.setReadOnly(readOnly);
		environmentConfig.setAllowCreate(!readOnly);
		environmentConfig.setTransactional(!readOnly);
		environment = new Environment(dbFile, environmentConfig);

		final DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setReadOnly(readOnly);
		databaseConfig.setAllowCreate(!readOnly);
		databaseConfig.setTransactional(!readOnly);

		database = environment.openDatabase(null, "MyDB", databaseConfig);
	}

	/** {@inheritDoc} */
	public void stop() {
		try {
			doStop();
		} catch (final DatabaseException e) {
			throw new VRuntimeException(e);
		}
	}

	/**
	 * Fermeture de la DB.
	 */
	private void doStop() throws DatabaseException {
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
		final KTransaction transaction = getTransactionManager().getCurrentTransaction();
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
	//		//			final D item = getPersistenceManager().getBroker().<D> get(uri);
	//		//			//-------------
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
	//		//----------------------------------------------------------------------
	//		return obtainBerkeleyResource().obtainBerkeleyReader().loadDtList(dtDefinition, maxRows);
	//		//final List<String> list = obtainLuceneResource().obtainLuceneReader().getURINameList(dtDefinition, ((SimpleCriteria) criteria).getSearch(), maxRows);
	//		//final DtList<D> dtc = new DtList<D>(dtDefinition);
	//		//		for (final String urn : list) {
	//		//			final DtObjectURI uri = URIHelper.readURN(urn);
	//		//			try {
	//		//				final D item = getPersistenceManager().getBroker().<D> get(uri);
	//		//				//-------------
	//		//				dtc.add(item);
	//		//			} catch (final Exception e) {
	//		//			}
	//		//		}
	//		//return dtc;
	//	}

	public void delete(final String id) {
		berkeleyDatabase.delete(id);
	}

	public void put(final String id, final Object object) {
		berkeleyDatabase.put(id, object);
	}

	public <C> Option<C> find(final String id, final Class<C> clazz) {
		return berkeleyDatabase.find(id, clazz);
	}

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
