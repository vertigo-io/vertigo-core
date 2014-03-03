package io.vertigo.dynamo.impl.work.listener;

/**
 * Interface de réception des événements produits par l'exécution des taches.
 *
 * @author pchretien
 * @version $Id: WorkListener.java,v 1.3 2013/11/15 15:33:20 pchretien Exp $
 */
public interface WorkListener {
	/**
	 * Enregistre le début d'exécution d'une tache.
	 * @param workName Nom de la tache 
	 */
	void onStart(String workName);

	/**
	 * Enregistre la fin  d'exécution d'une tache avec le temps d'exécution en ms et son statut (OK/KO).
	 * @param workName Nom de la tache exécutée
	 * @param elapsedTime Temps d'exécution en ms
	 * @param success Si la tache a été correctement executée
	 */
	void onFinish(String workName, long elapsedTime, boolean success);
}
