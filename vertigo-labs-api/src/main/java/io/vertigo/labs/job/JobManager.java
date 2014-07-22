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

import io.vertigo.kernel.component.Manager;
import io.vertigo.labs.job.metamodel.JobDefinition;

import java.util.Date;

/**
 * Job scheduler.
 * Cette classe permet d'ex�cuter une "t�che" de mani�re ind�pendante d'une servlet :
 * - � une fr�quence fixe (ex : toutes les 10 min)
 * - � une heure fixe chaque jour (ex : � 1h tous les jours)
 * - de suite (ex : d�s que possible s'il n'y a pas d�j� une t�che en cours de m�me nom)
 *
 * En g�n�ral, les impl�mentations de cette interface utilisent un ou plusieurs threads s�par�s
 * du thread d'appel et sont transactionnelles dans leurs threads pour chaque ex�cution.
 *
 * @author evernat
 * @version $Id: JobManager.java,v 1.2 2013/10/22 10:55:30 pchretien Exp $
 */
public interface JobManager extends Manager {
	/**
	 * Programme un job pour ex�cution � une fr�quence donn�e en secondes.
	 * @param periodInSecond Fr�quence d'ex�cution en secondes
	 */
	void scheduleEverySecondInterval(final JobDefinition jobDefinition, int periodInSecond);

	/**
	 * Programme un job pour ex�cution chaque jour � heure fixe.
	 * <br/>Si il y a besoin de programmer un job pour ex�cution � jour fixe dans la semaine
	 * ou dans le mois, il peut �tre programm� un job chaque puis conditioner l'ex�cution selon la
	 * date courante en utilisant la classe Calendar.
	 * @param hour Heure fixe d'ex�cution
	 */
	void scheduleEveryDayAtHour(final JobDefinition jobDefinition, int hour);

	/**
	 * Programme un job pour une seul ex�cution � une date donn�e.
	 * @param date Date d'ex�cution
	 */
	void scheduleAtDate(final JobDefinition jobDefinition, Date date);

	/**
	 * Ex�cution imm�diate et asynchrone d'un job.
	 */
	void scheduleNow(final JobDefinition jobDefinition);

	/**
	 * Ex�cution imm�diate et synchrone d'un job.
	 */
	void execute(final JobDefinition jobDefinition);
}
