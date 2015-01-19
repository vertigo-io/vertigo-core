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
package io.vertigo.dynamo.impl.work.listener;

import io.vertigo.dynamo.work.WorkManager;

import org.apache.log4j.Logger;

/**
 * Implémentation standard du Listener de réception des événements produits par l'exécution des taches.
 * @author pchretien
 */
public final class WorkListenerImpl implements WorkListener {

	/** Mécanisme de log utilisé pour les taches. */
	private static final Logger LOGGER = Logger.getLogger(WorkManager.class);

	/** Mécanisme de log utilisé pour les performances. */
	private static final Logger LOGGER_PERFORMANCE = Logger.getLogger("Performance");

	private void logWorkStart(final String workName) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Execution tache : " + workName);
		}
	}

	private void logWorkFinish(final String workName, final long elapsedTime, final boolean success) {
		if (LOGGER_PERFORMANCE.isInfoEnabled()) {
			LOGGER_PERFORMANCE.info(">> Tache : " + workName + " : time = " + elapsedTime);
		}
		if (LOGGER.isInfoEnabled()) {
			if (success) {
				LOGGER.info("Execution tache : " + workName + " reussie en  ( " + elapsedTime + " ms)");
			} else {
				LOGGER.info("Execution tache : " + workName + " interrompue apres ( " + elapsedTime + " ms)");
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onStart(final String workName) {
		//		analyticsManager.getAgent().startProcess(PROCESS_TYPE, workName);
		logWorkStart(workName);
	}

	/** {@inheritDoc} */
	@Override
	public void onFinish(final String workName, final long elapsedTime, final boolean success) {
		//		analyticsManager.getAgent().setMeasure(ERROR_PCT, success ? 0 : 100);
		//		analyticsManager.getAgent().stopProcess();
		logWorkFinish(workName, elapsedTime, success);
	}
}
