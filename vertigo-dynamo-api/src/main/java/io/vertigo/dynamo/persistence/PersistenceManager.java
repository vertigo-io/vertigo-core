package io.vertigo.dynamo.persistence;

import io.vertigo.kernel.component.Manager;

/**
* Gestionnaire des données et des accès aux données.
*
* @author pchretien
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
