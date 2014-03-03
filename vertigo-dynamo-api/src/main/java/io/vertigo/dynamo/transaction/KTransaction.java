package io.vertigo.dynamo.transaction;

/**
 * Transaction.
 * Une transaction :
 * - contient un ensemble de ressources de type BDD, fichiers, JMS...
 * - est soit démarrée, soit terminée.
 * - peut posséder (ou être) une transaction imbriquée.
 *
 * @author  pchretien
 * @version $Id: KTransaction.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public interface KTransaction {
	/**
	 * Ajoute une ressource à la transaction en précisant son ordre au sein de la transaction.
	 * Il n'est pas possible d'enregistrer pour une même transaction, deux ressources avec le même identifiant.
	 * @param id Identifiant de la ressource transactionnelle au sein de la transaction
	 * @param resource Ressource transactionnelle
	 * @param <TR> Ressource transactionnelle
	 */
	<TR extends KTransactionResource> void addResource(KTransactionResourceId<TR> id, TR resource);

	/**
	 * @param transactionResourceId Identifiant/type de ressource transactionnelle.
	 * @return Ressource transactionnelle correspondant à l'id
	 * @param <TR> Ressource transactionnelle
	 */
	<TR extends KTransactionResource> TR getResource(KTransactionResourceId<TR> transactionResourceId);
}
