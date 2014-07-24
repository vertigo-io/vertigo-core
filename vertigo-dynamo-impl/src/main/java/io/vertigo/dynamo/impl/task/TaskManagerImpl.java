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
package io.vertigo.dynamo.impl.task;

import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

/**
 * @author  pchretien
 */
public final class TaskManagerImpl implements TaskManager {
	private final WorkManager workManager;

	@Inject
	public TaskManagerImpl(WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public TaskResult execute(Task task) {
		return workManager.process(task, new WorkEngineProvider<>(task.getDefinition().getTaskEngineClass()));
	}
}
