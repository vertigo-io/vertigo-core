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
package io.vertigo.tempo.job;

import io.vertigo.kernel.component.Manager;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import java.util.Date;

/**
 * Job scheduler.
 * Cette classe permet d'exécuter une "tâche" de manière indépendante d'une servlet :
 * - à une fréquence fixe (ex : toutes les 10 min)
 * - à une heure fixe chaque jour (ex : à 1h tous les jours)
 * - de suite (ex : dés que possible s'il n'y a pas déjà une tâche en cours de même nom)
 *
 * En général, les implémentations de cette interface utilisent un ou plusieurs threads séparés
 * du thread d'appel et sont transactionnelles dans leurs threads pour chaque exécution.
 *
 * @author evernat
 */
public interface JobManager extends Manager {
	/**
	 * Programme un job pour exécution à une fréquence donnée en secondes.
	 * @param periodInSecond Fréquence d'exécution en secondes
	 */
	void scheduleEverySecondInterval(final JobDefinition jobDefinition, int periodInSecond);

	/**
	 * Programme un job pour exécution chaque jour à heure fixe.
	 * <br/>Si il y a besoin de programmer un job pour exécution à jour fixe dans la semaine
	 * ou dans le mois, il peut être programmé un job chaque puis conditioner l'exécution selon la
	 * date courante en utilisant la classe Calendar.
	 * @param hour Heure fixe d'exécution
	 */
	void scheduleEveryDayAtHour(final JobDefinition jobDefinition, int hour);

	/**
	 * Programme un job pour une seul exécution à une date donnée.
	 * @param date Date d'exécution
	 */
	void scheduleAtDate(final JobDefinition jobDefinition, Date date);

	/**
	 * Exécution immédiate et asynchrone d'un job.
	 */
	void scheduleNow(final JobDefinition jobDefinition);

	/**
	 * Exécution immédiate et synchrone d'un job.
	 */
	void execute(final JobDefinition jobDefinition);
}
