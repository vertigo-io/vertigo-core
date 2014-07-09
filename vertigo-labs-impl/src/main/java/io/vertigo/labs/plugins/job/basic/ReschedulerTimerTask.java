package io.vertigo.labs.plugins.job.basic;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.job.JobDefinition;
import io.vertigo.labs.job.JobManager;

import java.util.TimerTask;

/**
 * Timer permettant de reprogrammer un job.
 * @author npiedeloup
 * @version $Id: ReschedulerTimerTask.java,v 1.3 2013/10/22 10:55:50 pchretien Exp $
 */
final class ReschedulerTimerTask extends TimerTask {
	private final JobManager jobManager;
	private final JobDefinition jobDefinition;
	private final int hour;

	/**
	 * Constructeur.
	 * @param jobManager Manager des job
	 * @param jobDefinition D�finition du job � reprogrammer
	 * @param hour Heure du prochaine lancement
	 */
	ReschedulerTimerTask(final JobManager jobManager, final JobDefinition jobDefinition, final int hour) {
		Assertion.checkNotNull(jobManager);
		Assertion.checkNotNull(jobDefinition);
		//---------------------------------------------------------------------
		this.jobManager = jobManager;
		this.jobDefinition = jobDefinition;
		this.hour = hour;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		// pour un job s'ex�cutant tous les jours, on schedule chaque jour
		// pour �viter que l'ex�cution se d�cale d'une heure lors des changements d'heure �t�-hiver

		// On rappel le scheduleEveryDayAtHour qui reprogrammera � la fois la prochaine task du Job et celle du ReschedulerTimerTask.
		jobManager.scheduleEveryDayAtHour(jobDefinition, hour);
	}
}