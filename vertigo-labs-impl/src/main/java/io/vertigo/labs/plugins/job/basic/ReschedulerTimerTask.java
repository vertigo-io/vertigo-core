/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.labs.plugins.job.basic;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.job.JobManager;
import io.vertigo.labs.job.metamodel.JobDefinition;

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
	 * @param jobDefinition Définition du job à reprogrammer
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
		// pour un job s'exécutant tous les jours, on schedule chaque jour
		// pour éviter que l'exécution se décale d'une heure lors des changements d'heure été-hiver

		// On rappel le scheduleEveryDayAtHour qui reprogrammera à la fois la prochaine task du Job et celle du ReschedulerTimerTask.
		jobManager.scheduleEveryDayAtHour(jobDefinition, hour);
	}
}
