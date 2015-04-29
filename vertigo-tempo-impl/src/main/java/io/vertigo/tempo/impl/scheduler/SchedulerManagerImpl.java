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
package io.vertigo.tempo.impl.scheduler;

import io.vertigo.lang.Assertion;
import io.vertigo.tempo.job.metamodel.JobDefinition;
import io.vertigo.tempo.scheduler.SchedulerManager;

import java.util.Date;

import javax.inject.Inject;

/**
 * Impl of SchedulerManager.
 * @author pchretien
 */
public final class SchedulerManagerImpl implements SchedulerManager {
	private final SchedulerPlugin schedulerPlugin;

	/**
	 * Constructor.
	 */
	@Inject
	public SchedulerManagerImpl(final SchedulerPlugin schedulerPlugin) {
		Assertion.checkNotNull(schedulerPlugin);
		//-----
		this.schedulerPlugin = schedulerPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEverySecondInterval(final JobDefinition jobDefinition, final int periodInSecond) {
		schedulerPlugin.scheduleEverySecondInterval(jobDefinition, periodInSecond);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleEveryDayAtHourMinute(final JobDefinition jobDefinition, final int hour, final int minute) {
		schedulerPlugin.scheduleEveryDayAtHourMinute(jobDefinition, hour, minute);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleAtDate(final JobDefinition jobDefinition, final Date date) {
		schedulerPlugin.scheduleAtDate(jobDefinition, date);
	}

	/** {@inheritDoc} */
	@Override
	public void scheduleNow(final JobDefinition jobDefinition) {
		schedulerPlugin.scheduleNow(jobDefinition);
	}
}
