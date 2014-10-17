package io.vertigo.dynamock.dao.fileinfo;

import javax.inject.Inject;

import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.impl.persistence.util.DAOBroker;

/**
 * DAO : Accès à un object (DTO, DTC). 
 * KxFileInfoDAO
 */
public final class KxFileInfoDAO extends DAOBroker<io.vertigo.dynamock.domain.fileinfo.KxFileInfo, java.lang.Long> {

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 */
	@Inject
	public KxFileInfoDAO(final PersistenceManager persistenceManager) {
		super(io.vertigo.dynamock.domain.fileinfo.KxFileInfo.class, persistenceManager);
	}
}
