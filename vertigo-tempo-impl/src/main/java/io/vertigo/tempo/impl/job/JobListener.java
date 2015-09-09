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
package io.vertigo.tempo.impl.job;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Listener of the execution of the jobs.
 * @author pchretien
 */
final class JobListener {
	/** Type de process gérant les statistiques des jobs. */
	private static final String PROCESS_TYPE = "JOB";

	/** Mesures des exceptions utilisateur. */
	private static final String ME_USER_ERROR_PCT = "ME_USER_ERROR_PCT";
	/** Mesures des exceptions system. */
	private static final String ME_ERROR_PCT = "ME_ERROR_PCT";

	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 */
	@Inject
	public JobListener(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	void onStart(final JobDefinition jobDefinition) {
		analyticsManager.getAgent().startProcess(PROCESS_TYPE, jobDefinition.getName());
		analyticsManager.getAgent().setMeasure(ME_USER_ERROR_PCT, 0d);
		analyticsManager.getAgent().setMeasure(ME_ERROR_PCT, 0d);
		getLogger(jobDefinition.getName()).info("Exécution du job " + jobDefinition.getName());
	}

	void onFinish(final JobDefinition jobDefinition, final long timeMillisSeconds) {
		getLogger(jobDefinition.getName()).info("Job " + jobDefinition.getName() + " exécuté en " + (timeMillisSeconds) + " ms");
		analyticsManager.getAgent().stopProcess();
	}

	void onFinish(final JobDefinition jobDefinition, final Throwable throwable) {
		// On catche throwable et pas seulement exception pour que le timer
		// ne s'arrête pas en cas d'Assertion ou de OutOfMemoryError :
		// Aucune exception ou erreur ne doit être lancée par la méthode doExecute
		getLogger(jobDefinition.getName()).warn(throwable.toString(), throwable);

		if (isUserException(throwable)) {
			analyticsManager.getAgent().setMeasure(ME_USER_ERROR_PCT, 100d);
		} else {
			analyticsManager.getAgent().setMeasure(ME_ERROR_PCT, 100d);
		}
		analyticsManager.getAgent().addMetaData("ME_ERROR_HEADER", String.valueOf(throwable));
	}

	private static boolean isUserException(final Throwable t) {
		return t instanceof VUserException;
	}

	private static Logger getLogger(final String jobName) {
		return Logger.getLogger(jobName);
	}

}
