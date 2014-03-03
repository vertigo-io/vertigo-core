package io.vertigo.dynamo.transaction;

import io.vertigo.kernel.component.Manager;

/**
 * Gestionnaire de transactions.
 * Une transaction contient un ensemble de ressources de type 
 *  - BDD, 
 *  - fichiers, 
 *  - JMS... 
 *  
 * Le Manager permet :
 * - Soit d'obtenir la transaction courante (la créant au besoin),
 * - Soit de créer une transaction autonome (au sein de la transaction courante).
 *
 * @author  pchretien
 * @version $Id: KTransactionManager.java,v 1.2 2013/10/22 12:31:28 pchretien Exp $
 */
public interface KTransactionManager extends Manager {
	/**
	 * Crée la transaction courante.
	 * Il est nécessaire qu'aucune transaction courante vivante n'existe.
	 * @return Transaction courante.
	 */
	KTransactionWritable createCurrentTransaction();

	/**
	 * Crée une transaction autonome sous la transaction courante déjà démarrée.
	 * Il est impératif qu'une transaction courante vivante existe.
	 * Cette transaction deviendra la transaction courante et devra être commitée ou rollbackée
	 * avant d'agir sur la transaction parente.
	 * @return Nouvelle transaction courante
	 */
	KTransactionWritable createAutonomousTransaction();

	/**
	 * Récupère la transaction courante.
	 * Il est nécessaire que cette transaction existe.
	 * @return Transaction courante.
	 */
	KTransaction getCurrentTransaction();

	/**
	 * Indique si une transaction courante existe.
	 * @return Si il existe une transcation courante.
	 */
	boolean hasCurrentTransaction();
}
