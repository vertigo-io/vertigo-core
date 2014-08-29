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
package io.vertigo.dynamo.impl.task.listener;

/**
 * Interface de réception des événements produits par l'exécution des taches.
 *
 * @author pchretien
 */
public interface TaskListener {
	/**
	 * Enregistre le début d'exécution d'une tache.
	 * @param taskName Nom de la tache 
	 */
	void onStart(String taskName);

	/**
	 * Enregistre la fin  d'exécution d'une tache avec le temps d'exécution en ms et son statut (OK/KO).
	 * @param taskName Nom de la tache exécutée
	 * @param elapsedTime Temps d'exécution en ms
	 * @param success Si la tache a été correctement executée
	 */
	void onFinish(String taskName, long elapsedTime, boolean success);
}
