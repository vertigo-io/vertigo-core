package io.vertigo.dynamo.work;

/**
 * Hanlder permettant de définir le comportement après exécution asynchrone d'un work.
 * 
 * @author   pchretien, npiedeloup
 */
public interface WorkResultHandler<WR> {
	/**
	 * Démarrage de l'exécution de la tache.
	 * Notification pour information.
	 */
	void onStart();

	/**
	 * Exécution terminée avec succès.
	 * @param result Résultat de l'excution 
	 */
	void onSuccess(final WR result);

	/**
	 * Exécution terminée par la survenue d'une exception
	 * @param error  Exception
	 */
	void onFailure(final Throwable error);

	/**
	 * Exécution interrompue par un timeout
	 * 
	 * @param timeoutInSeconds TimeoutIn Seconds exprimé en secondes
	 */
	//void onTimeout(final int timeoutInSeconds);
}
