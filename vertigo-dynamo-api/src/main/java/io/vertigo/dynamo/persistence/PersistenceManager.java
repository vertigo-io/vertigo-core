package io.vertigo.dynamo.persistence;

import io.vertigo.kernel.component.Manager;

/**
* Gestionnaire des données et des accès aux données.
*
* @author pchretien
* @version $Id: PersistenceManager.java,v 1.2 2013/10/22 12:37:38 pchretien Exp $
*/
public interface PersistenceManager extends Manager {
	/**
	 * @return Broker d'objets métier
	 */
	Broker getBroker();

	//
	BrokerNN getBrokerNN();

	/**
	 * @return Configuration du composant de persistance
	 */
	BrokerConfiguration getBrokerConfiguration();

	/**
	 * @return Configuration MDM
	 */
	MasterDataConfiguration getMasterDataConfiguration();
}
