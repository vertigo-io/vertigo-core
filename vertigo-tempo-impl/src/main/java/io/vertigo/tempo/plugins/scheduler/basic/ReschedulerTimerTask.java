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
package io.vertigo.tempo.plugins.scheduler.basic;

import java.util.TimerTask;

import io.vertigo.lang.Assertion;
import io.vertigo.tempo.impl.scheduler.SchedulerPlugin;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Timer permettant de reprogrammer un job.
 * @author npiedeloup
 */
final class ReschedulerTimerTask extends TimerTask {
	private final SchedulerPlugin schedulerPlugin;
	private final JobDefinition jobDefinition;
	private final int hour;
	private final int minute;

	/**
	 * Constructeur.
	 * @param schedulerPlugin Scheduler
	 * @param jobDefinition Définition du job à reprogrammer
	 * @param hour Heure du prochaine lancement
	 * @param minute Minute du prochaine lancement
	 */
	ReschedulerTimerTask(final SchedulerPlugin schedulerPlugin, final JobDefinition jobDefinition, final int hour, final int minute) {
		Assertion.checkNotNull(schedulerPlugin);
		Assertion.checkNotNull(jobDefinition);
		//-----
		this.schedulerPlugin = schedulerPlugin;
		this.jobDefinition = jobDefinition;
		this.hour = hour;
		this.minute = minute;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		// pour un job s'exécutant tous les jours, on schedule chaque jour
		// pour éviter que l'exécution se décale d'une heure lors des changements d'heure été-hiver

		// On rappel le scheduleEveryDayAtHour qui reprogrammera à la fois la prochaine task du Job et celle du ReschedulerTimerTask.
		schedulerPlugin.scheduleEveryDayAtHourMinute(jobDefinition, hour, minute);
	}
}
