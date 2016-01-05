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
package io.vertigo.tempo.scheduler;

import io.vertigo.lang.Manager;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import java.util.Date;

/**
 * Scheduler.
 * This component triggers the execution of jobs
 * - at a rate frequency (ex : each 10 min)
 * - at a fix hour each day (ex : at 1h am each day except ... )
 *
 * @author pchretien s
 */
public interface SchedulerManager extends Manager {
	/**
	 * Schedules a job at a rate frequency defined in seconds.
	 * 
	 * @param jobDefinition the type of job 
	 * @param periodInSecond Fréquence d'exécution en secondes
	 */
	void scheduleEverySecondInterval(final JobDefinition jobDefinition, int periodInSecond);

	/**
	 * Programme un job pour exécution chaque jour à heure et minute fixe.
	 * <br/>Si il y a besoin de programmer un job pour exécution à jour fixe dans la semaine
	 * ou dans le mois, il peut être programmé un job chaque puis conditioner l'exécution selon la
	 * date courante en utilisant la classe Calendar.
	 * 
	 * @param jobDefinition the type of job 
	 * @param hour Heure fixe d'exécution
	 * @param minute Minute fixe d'exécution
	 */
	void scheduleEveryDayAtHourMinute(final JobDefinition jobDefinition, int hour, int minute);

	/**
	 * Programme un job pour une seul exécution à une date donnée.
	 * 
	 * @param jobDefinition the type of job 
	 * @param date Date d'exécution
	 */
	void scheduleAtDate(final JobDefinition jobDefinition, Date date);

	/**
	 * Exécution immédiate et asynchrone d'un job.
	 * 
	 * @param jobDefinition the type of job 
	 */
	void scheduleNow(final JobDefinition jobDefinition);
}
