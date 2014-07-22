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
package io.vertigo.labs.job;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.labs.job.metamodel.JobDefinition;

import java.util.Date;

/**
 * Plugin de lancement des Jobs.
 * @author pchretien
 * @version $Id: SchedulerPlugin.java,v 1.2 2013/10/22 10:55:30 pchretien Exp $
 */
public interface SchedulerPlugin extends Plugin {
	/**
	 * Programme un job pour ex�cution � une fr�quence donn�e en secondes.
	 * @param periodInSecond Fr�quence d'ex�cution en secondes
	 */
	void scheduleEverySecondInterval(final JobManager jobManager, final JobDefinition jobDefinition, int periodInSecond);

	/**
	 * Programme un job pour ex�cution chaque jour � heure fixe.
	 * <br/>Si il y a besoin de programmer un job pour ex�cution � jour fixe dans la semaine
	 * ou dans le mois, il peut �tre programm� un job chaque puis conditioner l'ex�cution selon la
	 * date courante en utilisant la classe Calendar.
	 * @param hour Heure fixe d'ex�cution
	 */
	void scheduleEveryDayAtHour(final JobManager jobManager, final JobDefinition jobDefinition, int hour);

	/**
	 * Programme un job pour une seul ex�cution � une date donn�e.
	 * @param date Date d'ex�cution
	 */
	void scheduleAtDate(final JobManager jobManager, final JobDefinition jobDefinition, Date date);

	/**
	 * Programme un job pour une seul ex�cution imm�diate.
	 */
	void scheduleNow(final JobManager jobManager, final JobDefinition jobDefinition);
}
