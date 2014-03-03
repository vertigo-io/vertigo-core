package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

/**
 * Etat d'avancement d'un Work executé de façon asynchrone.
 * 
 * Cet objet interne 
 * - est partagé entre le donneur d'ordre et le worker.
 * - il permet d'avoir une vision de l'état du travail en cours
 * 
 * @author pchretien
 * @version $Id: WorkItem.java,v 1.6 2013/11/15 15:33:20 pchretien Exp $
 */
public final class WorkItem<WR, W> {
	private final W work;
	private final WorkResultHandler<WR> workResultHandler;
	private final WorkEngineProvider<WR, W> workEngineProvider;

	/**
	 * Constructeur.
	 * @param work Travail dont on représente l'état.
	 */
	public WorkItem(final W work, final WorkEngineProvider<WR, W> workEngineProvider, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(work);
		Assertion.checkNotNull(workEngineProvider);
		Assertion.checkNotNull(workResultHandler);
		//---------------------------------------------------------------------
		this.work = work;
		this.workResultHandler = workResultHandler;
		this.workEngineProvider = workEngineProvider;
	}

	/**
	 * Permet de récupérer les informations pour réaliser un traitement. 
	 * @return le work
	 */
	public W getWork() {
		return work;
	}

	/**
	 * Permet de récupérer le WorkResultHandler traitant les resultats de l'éxecution. 
	 * @return le work
	 */
	public WorkResultHandler<WR> getWorkResultHandler() {
		return workResultHandler;
	}

	public WorkEngineProvider<WR, W> getWorkEngineProvider() {
		return workEngineProvider;
	}

}
