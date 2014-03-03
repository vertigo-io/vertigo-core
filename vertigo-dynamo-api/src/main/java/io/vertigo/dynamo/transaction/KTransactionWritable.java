package io.vertigo.dynamo.transaction;

/**
 * Transaction.
 * Soit on commit, soit on rollback une transaction. 
 * Le commit ou le rollback est propagé sur toutes les ressources participant à la transaction.
 * Pour des raisons de simplicité on se refuse à utiliser le commit à 2 phases.
 * Les ressources sont commitées selon leur priorités.	
 * La transaction possède un état interne qui est modifié de façon irréversible lors du commit ou du rollback.
 * Une transaction est soit démarrée, soit terminée.
 *
 * Une transaction peut posséder (ou être) une transaction imbriquée.
 *
 * @author  pchretien
 * @version $Id: KTransactionWritable.java,v 1.2 2013/07/29 11:45:50 pchretien Exp $
 */
public interface KTransactionWritable extends KTransaction, AutoCloseable {
	/**
	 * Valide la transaction.
	 * Cette méthode commit puis libère dans l'ordre toutes les ressources participant à la transaction.
	 * Si aucune ressource n'est présente, cette méthode ne fait rien.	 *
	 */
	void commit();

	/**
	 * Annule la transaction.
	 * Cette méthode annule puis libère dans l'ordre toutes les ressources participant à la transaction.
	 *
	 * Si aucune ressource n'est présente,
	 * ou bien si un commit ou un rollback a déja fermé la transaction
	 * alors cette méthode ne fait rien.
	 */
	void rollback();

	//method is overriden to delete "throw Exception"
	//close is similar to rollback
	void close();

}
