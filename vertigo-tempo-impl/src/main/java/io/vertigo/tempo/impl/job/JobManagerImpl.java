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
import io.vertigo.tempo.job.JobManager;
import io.vertigo.tempo.job.SchedulerPlugin;
import io.vertigo.tempo.job.metamodel.JobDefinition;

import java.util.Date;

import javax.inject.Inject;

/**
 * Implémentation générique de JobManager.
 * Attention, cette implémentation n'est pas transactionnelle ; la gestion des transaction doit rester à la charge des services.
 *
 * Les jobs sont instanciés, y sont injectés les composants métiers et les managers.
 *
 * @author evernat, pchretien
 */
public final class JobManagerImpl implements JobManager/*, ManagerDescription*/{
	private final SchedulerPlugin schedulerPlugin;
	private final JobListener jobListener;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public JobManagerImpl(final AnalyticsManager analyticsManager, final SchedulerPlugin schedulerPlugin) {
		Assertion.checkNotNull(schedulerPlugin);
		//-----
		jobListener = new JobListener(analyticsManager);
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
		//-----
		jobListener.onStart(jobDefinition);
		final long start = System.currentTimeMillis();
		try {
			final Runnable job = createJob(jobDefinition);
			job.run(); //NOSONAR : JobManager should managed Job execution, it decided if a runnable job runs in a new thread or not
		} catch (final Throwable throwable) { //NOSONAR
			jobListener.onFinish(jobDefinition, throwable);
		} finally {
			jobListener.onFinish(jobDefinition, System.currentTimeMillis() - start);

		}
	}

	private static Runnable createJob(final JobDefinition jobDefinition) {
		return Injector.newInstance(jobDefinition.getJobClass(), Home.getComponentSpace());
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEverySecondInterval(final JobDefinition jobDefinition, final int periodInSecond) {
		schedulerPlugin.scheduleEverySecondInterval(this, jobDefinition, periodInSecond);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEveryDayAtHourMinute(final JobDefinition jobDefinition, final int hour, final int minute) {
		schedulerPlugin.scheduleEveryDayAtHourMinute(this, jobDefinition, hour, minute);
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
}
