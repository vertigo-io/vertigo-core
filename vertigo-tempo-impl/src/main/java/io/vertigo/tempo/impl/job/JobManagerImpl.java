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
import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VUserException;
import io.vertigo.tempo.job.JobManager;
import io.vertigo.tempo.job.SchedulerPlugin;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;

/**
 * Implémentation générique de JobManager.
 * Attention, cette implémentation n'est pas transactionnelle ; la gestion des transaction doit rester à la charge des services.
 *
 * Les jobs sont instanciés, y sont injectés les composants métiers et les managers.
 *
 * @author evernat, pchretien
 */
public final class JobManagerImpl implements JobManager/*, ManagerDescription*/{
	/** Type de process gérant les statistiques des jobs. */
	private static final String PROCESS_TYPE = "JOB";

	/** Mesures des exceptions utilisateur. */
	private static final String JOB_USER_EXCEPTION_COUNT = "JOB_USER_EXCEPTION_COUNT";
	/** Mesures des exceptions system. */
	private static final String JOB_SYSTEM_EXCEPTION_COUNT = "JOB_SYSTEM_EXCEPTION_COUNT";

	private final SchedulerPlugin schedulerPlugin;
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public JobManagerImpl(final AnalyticsManager analyticsManager, final SchedulerPlugin schedulerPlugin) {
		Assertion.checkNotNull(analyticsManager);
		Assertion.checkNotNull(schedulerPlugin);
		//-----
		this.analyticsManager = analyticsManager;
		this.schedulerPlugin = schedulerPlugin;
		//A déplacer
		//A déplacer
		//A déplacer
		//A déplacer
		Home.getDefinitionSpace().register(JobDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public void execute(final JobDefinition jobDefinition) {
		analyticsManager.getAgent().startProcess(PROCESS_TYPE, jobDefinition.getName());
		try {
			doExecute(jobDefinition);
		} catch (final Throwable throwable) { //NOSONAR
			// On catche throwable et pas seulement exception pour que le timer
			// ne s'arrête pas en cas d'Assertion ou de OutOfMemoryError :
			// Aucune exception ou erreur ne doit être lancée par la méthode doExecute
			getLogger(jobDefinition.getName()).warn(throwable.toString(), throwable);

			if (isUserException(throwable)) {
				analyticsManager.getAgent().setMeasure(JOB_USER_EXCEPTION_COUNT, 100);
			} else {
				analyticsManager.getAgent().setMeasure(JOB_SYSTEM_EXCEPTION_COUNT, 100);
			}
		} finally {
			analyticsManager.getAgent().stopProcess();
		}
	}

	/**
	 * Gestion de l'exécution d'un Job avec son log.
	 */
	private static void doExecute(final JobDefinition jobDefinition) {
		final Runnable job = Injector.newInstance(jobDefinition.getJobClass(), Home.getComponentSpace());

		final long start = System.currentTimeMillis();
		getLogger(jobDefinition.getName()).info("Exécution du job " + jobDefinition.getName());
		try {
			job.run(); //NOSONAR : JobManager should managed Job execution, it decided if a runnable job runs in a new thread or not
		} finally {
			final long end = System.currentTimeMillis();
			getLogger(jobDefinition.getName()).info("Job " + jobDefinition.getName() + " exécuté en " + (end - start) + " ms");
		}
	}

	private static Logger getLogger(final String jobName) {
		return Logger.getLogger(jobName);
	}

	private static boolean isUserException(final Throwable t) {
		return t instanceof VUserException;
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEverySecondInterval(final JobDefinition jobDefinition, final int periodInSecond) {
		schedulerPlugin.scheduleEverySecondInterval(this, jobDefinition, periodInSecond);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEveryDayAtHour(final JobDefinition jobDefinition, final int hour) {
		schedulerPlugin.scheduleEveryDayAtHour(this, jobDefinition, hour);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleAtDate(final JobDefinition jobDefinition, final Date date) {
		schedulerPlugin.scheduleAtDate(this, jobDefinition, date);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleNow(final JobDefinition jobDefinition) {
		schedulerPlugin.scheduleNow(this, jobDefinition);
	}

	// /** {@inheritDoc} */
	// public void toHtml(final PrintStream out) throws Exception {
	// analyticsManager.getDashboard().toHtml(DB, out);
	// }
	//
	// /** {@inheritDoc} */
	// public final String getName() {
	// return "Job";
	// }
	//
	// /** {@inheritDoc} */
	// public final String getImage() {
	// return "cpanel.png";
	// }

}
