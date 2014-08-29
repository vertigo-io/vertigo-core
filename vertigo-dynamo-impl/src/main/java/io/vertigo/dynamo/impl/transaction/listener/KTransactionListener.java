package io.vertigo.dynamo.impl.transaction.listener;

public interface KTransactionListener {
	/**
	 * Enregistre le début d'une transaction.
	 */
	void onTransactionStart();

	/**
	 * Fin de transaction.
	 * @param rollback Si la transaction a réussie ou rollbackée
	 * @param elapsedTime Temps d'exécution en ms
	 */
	void onTransactionFinish(boolean rollback, long elapsedTime);
}
