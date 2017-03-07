/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.lang.Assertion;
import io.vertigo.tempo.job.JobManager;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Impl of JobManager.
 *
 * Jobs are built by DI using injection of the current components (defined in ComponentSpace).
 *
 * @author pchretien
 */
public final class JobManagerImpl implements JobManager {
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param analyticsManager Application Performance Manager.
	 */
	@Inject
	public JobManagerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(final JobDefinition jobDefinition) {
		//-----
		analyticsManager.trace(
				"jobs",
				jobDefinition.getName(),
				tracer -> createJob(jobDefinition).run());
	}

	private static Runnable createJob(final JobDefinition jobDefinition) {
		return DIInjector.newInstance(jobDefinition.getJobClass(), Home.getApp().getComponentSpace());
	}
}
