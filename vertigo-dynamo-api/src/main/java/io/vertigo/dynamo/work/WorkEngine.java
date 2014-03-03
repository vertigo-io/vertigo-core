package io.vertigo.dynamo.work;


/**
 * Moteur d'exécution d'un travail.
 * Le moteur N'EST PAS THREADSAFE ; il doit donc être instancié à chaque utilisation.
 * Le moteur est créé par Injection de dépendances.
 *  
 * @param <W> Type de Work (Travail)
 * @param <WR> Produit d'un work à l'issu de son exécution
 * @author   pchretien
 * @version $Id: WorkEngine.java,v 1.5 2013/11/15 15:43:35 pchretien Exp $
 */
public interface WorkEngine<WR, W> {
	/**
	 * Exécute le travail.
	 * Le travail s'exécute dans la transaction courante si elle existe.
	 *  - Le moteur n'est pas responsable de de créer une transaction.
	 *  - En revanche si une telle transaction existe elle est utilisée.
	 * @param work paramétrage du WorkEngine
	 * @return WorkResult contenant les résultats
	 */
	WR process(W work);
}
