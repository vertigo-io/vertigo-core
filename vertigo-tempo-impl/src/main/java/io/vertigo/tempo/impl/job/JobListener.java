package io.vertigo.tempo.impl.job;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Listener of the execution of the jobs.
 * @author pchretien
 */
final class JobListener {
	/** Type de process gérant les statistiques des jobs. */
	private static final String PROCESS_TYPE = "JOB";

	/** Mesures des exceptions utilisateur. */
	private static final String JOB_USER_EXCEPTION_COUNT = "JOB_USER_EXCEPTION_COUNT";
	/** Mesures des exceptions system. */
	private static final String JOB_SYSTEM_EXCEPTION_COUNT = "JOB_SYSTEM_EXCEPTION_COUNT";

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 */
	@Inject
	public JobListener(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	void onStart(final JobDefinition jobDefinition) {
		analyticsManager.getAgent().startProcess(PROCESS_TYPE, jobDefinition.getName());
		getLogger(jobDefinition.getName()).info("Exécution du job " + jobDefinition.getName());
	}

	void onFinish(final JobDefinition jobDefinition, final long timeMillisSeconds) {
		getLogger(jobDefinition.getName()).info("Job " + jobDefinition.getName() + " exécuté en " + (timeMillisSeconds) + " ms");
		analyticsManager.getAgent().stopProcess();
	}

	void onFinish(final JobDefinition jobDefinition, final Throwable throwable) {
		// On catche throwable et pas seulement exception pour que le timer
		// ne s'arrête pas en cas d'Assertion ou de OutOfMemoryError :
		// Aucune exception ou erreur ne doit être lancée par la méthode doExecute
		getLogger(jobDefinition.getName()).warn(throwable.toString(), throwable);

		if (isUserException(throwable)) {
			analyticsManager.getAgent().setMeasure(JOB_USER_EXCEPTION_COUNT, 100);
		} else {
			analyticsManager.getAgent().setMeasure(JOB_SYSTEM_EXCEPTION_COUNT, 100);
		} //
	}

	private static boolean isUserException(final Throwable t) {
		return t instanceof VUserException;
	}

	private static Logger getLogger(final String jobName) {
		return Logger.getLogger(jobName);
	}

}
