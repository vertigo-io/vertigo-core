package io.vertigo.dynamo.work;

import io.vertigo.kernel.component.Manager;

/**
 * Gestion synchrone, asynchrone des taches à effectuer.
 * Chaque réalisation est effectuée par un {@link WorkEngine}.
 * 
 * Les exécutions peuvent être 
 *  - locales ou distribuées (par configuration)
 *  - synchrones ou asynchrones (selon la méthode appelée)
 * 
 * Toutes les exécutions distribuées sont techniquement réalisées de façon asynchrones.
 * Dans le cas des appels synchrones de méthodes distribuées, un mécanisme resynchronise le résultat 
 * 
 * @author pchretien
 */
public interface WorkManager extends Manager {
	/**
	 * Exécution d'un travail de façon synchrone.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @return resultat
	 */
	<WR, W> void process(final WorkItem<WR, W> workItem);

	/**
	 * Lancement asynchrone d'un travail 'dès que possible'.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @param  workResultHandler Handler permettant un callback après exécution
	 */
	<WR, W> void schedule(final WorkItem<WR, W> workItem);
}
