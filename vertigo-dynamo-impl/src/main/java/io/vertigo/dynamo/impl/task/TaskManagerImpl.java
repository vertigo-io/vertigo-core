/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.impl.task;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.lang.Assertion;

/**
 * @author pchretien
 */
public final class TaskManagerImpl implements TaskManager {
	private final AnalyticsManager analyticsManager;

	/**
	 * @param analyticsManager Manager analytics
	 */
	@Inject
	public TaskManagerImpl(final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(analyticsManager);
		//-----
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public TaskResult execute(final Task task) {
		return analyticsManager
				.traceWithReturn(
						"tasks",
						"/execute/" + task.getDefinition().getName(),
						tracer -> doExecute(task));
	}

	private static TaskResult doExecute(final Task task) {
		final TaskEngine taskEngine = DIInjector.newInstance(task.getDefinition().getTaskEngineClass(), Home.getApp().getComponentSpace());
		return taskEngine.process(task);
	}
}
