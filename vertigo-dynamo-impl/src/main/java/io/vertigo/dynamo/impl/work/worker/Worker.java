package io.vertigo.dynamo.impl.work.worker;

import io.vertigo.dynamo.impl.work.worker.local.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;

/**
 * Interface d'un Worker threadsafe.
 * Permet d'exécuter un travail de façona * - synchrone
 * - asynchrone
 * 
 * @author pchretien, npiedeloup
 */
public interface Worker {
	/**
	 * Exécution d'un travail de façon synchrone.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @return resultat
	 */
	<WR, W> WR process(final W work, final WorkEngineProvider<WR, W> workEngineProvider);

	/**
	 * Exécution asynchrone d'un Work.
	 * - Si le traitement déclenche une exception le status est porté par WorkItem.
	 * - Si l'exécution asynchrone déclenche une exception, cela signifie qu'il est impossible de programmer le Work pour son exécution.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Tache, Work à exécuter
	 * @param  workResultHandler Handler permettant un callback après exécution
	 */
	<WR, W> void schedule(final WorkItem<WR, W> workItem);
}
