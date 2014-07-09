package io.vertigo.labs.plugins.job.basic;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.job.JobDefinition;
import io.vertigo.labs.job.JobManager;

import java.util.TimerTask;

/**
 * Timer permettant l'ex�cution d'un Job.
 * @author npiedeloup
 * @version $Id: BasicTimerTask.java,v 1.3 2013/10/22 10:55:50 pchretien Exp $
 */
final class BasicTimerTask extends TimerTask {
	private final JobDefinition jobDefinition;
	private final JobManager jobManager;

	/**
	 * Constructeur.
	 * @param jobManager Manager des jobs.
	 */
	BasicTimerTask(final JobDefinition jobDefinition, final JobManager jobManager) {
		Assertion.checkNotNull(jobDefinition);
		Assertion.checkNotNull(jobManager);
		//---------------------------------------------------------------------
		this.jobDefinition = jobDefinition;
		this.jobManager = jobManager;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		jobManager.execute(jobDefinition);
	}
}
