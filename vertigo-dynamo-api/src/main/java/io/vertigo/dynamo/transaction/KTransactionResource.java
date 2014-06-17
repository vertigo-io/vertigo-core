package io.vertigo.dynamo.transaction;

/**
 * Ressource participant à une transaction.
 * Cette ressource est par exemple :
 * - une connexion à une BDD Oracle, Sybase, MySQL....
 * - un mailer
 * - un fileSystem
 * - un objet java
 *
 * @author  pchretien
 */
public interface KTransactionResource {
	/**
	 * Valide la ressource.
	 * @throws Exception Si impossible.
	 */
	void commit() throws Exception;

	/**
	 * Annule la ressource.
	 * @throws Exception Si impossible.
	 */
	void rollback() throws Exception;

	/**
	 * Libère la ressource.
	 * Appelée systématiquement après un commit ou un rollback.
	 * @throws Exception Si impossible.
	 */
	void release() throws Exception;
}
