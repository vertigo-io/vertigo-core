package io.vertigo.dynamo.impl.work.listener;

import io.vertigo.dynamo.work.WorkManager;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des tachess.
 * 
 * @author pchretien
 */
public final class WorkListenerImpl implements WorkListener {
	/** Base de données gérant les statistiques des taches. */
	//	private static final String PROCESS_TYPE = "WORK";
	//	private static final String ERROR_PCT = "ERROR_PCT";

	//	private final AnalyticsManager analyticsManager;

	/** Mécanisme de log utilisé pour les taches. */
	private final Logger workLog;

	/** Mécanisme de log utilisé pour les performances. */
	private final Logger performanceLog;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de monitoring
	 */
	public WorkListenerImpl(/*final AnalyticsManager analyticsManager*/) {
		//		Assertion.checkNotNull(analyticsManager);
		//		//---------------------------------------------------------------------
		//		this.analyticsManager = analyticsManager;

		workLog = Logger.getLogger(WorkManager.class);
		performanceLog = Logger.getLogger("Performance");
	}

	private void logWorkStart(final String workName) {
		if (workLog.isDebugEnabled()) {
			workLog.debug("Execution tache : " + workName);
		}
	}

	private void logWorkFinish(final String workName, final long elapsedTime, final boolean success) {
		if (performanceLog.isInfoEnabled()) {
			performanceLog.info(">> Tache : " + workName + " : time = " + elapsedTime);
		}
		if (workLog.isInfoEnabled()) {
			if (success) {
				workLog.info("Execution tache : " + workName + " reussie en  ( " + elapsedTime + " ms)");
			} else {
				workLog.info("Execution tache : " + workName + " interrompue apres ( " + elapsedTime + " ms)");
			}
		}
	}

	/** {@inheritDoc} */
	public void onStart(final String workName) {
		//		analyticsManager.getAgent().startProcess(PROCESS_TYPE, workName);
		logWorkStart(workName);
	}

	/** {@inheritDoc} */
	public void onFinish(final String workName, final long elapsedTime, final boolean success) {
		//		analyticsManager.getAgent().setMeasure(ERROR_PCT, success ? 0 : 100);
		//		analyticsManager.getAgent().stopProcess();
		logWorkFinish(workName, elapsedTime, success);
	}
}
