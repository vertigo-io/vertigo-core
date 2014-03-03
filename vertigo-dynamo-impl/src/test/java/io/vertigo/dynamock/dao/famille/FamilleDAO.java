package io.vertigo.dynamock.dao.famille;

import io.vertigo.dynamo.impl.persistence.util.DAOBroker;
import io.vertigo.dynamo.persistence.PersistenceManager;

import javax.inject.Inject;

/**
 * DAO : accès à un object (DTO, DTC). 
 * FamilleDAO
 */
public final class FamilleDAO extends DAOBroker<io.vertigo.dynamock.domain.famille.Famille, java.lang.Long> {

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 */
	@Inject
	public FamilleDAO(final PersistenceManager persistenceManager) {
		super(io.vertigo.dynamock.domain.famille.Famille.class, persistenceManager);
	}
}
