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
package io.vertigo.dynamo.impl.node;

import io.vertigo.core.component.Plugin;
import io.vertigo.dynamo.impl.work.WorkItem;

import java.util.List;

/**
 * NodePlugin
 * @author pchretien
 */
public interface WorkerPlugin extends Plugin {
	/**
	 * @return Types of work, that can be done by this worker.
	 */
	List<String> getWorkTypes();

	/**
	 * Polling workitem. 
	 * @param workType 
	 * @param timeoutInSeconds
	 * @return Workitem or null (if timeout)
	 */
	<WR, W> WorkItem<WR, W> pollWorkItem(final String workType, final int timeoutInSeconds);

	/**
	 * Send result or error if execution failed
	 * @param workId WorkId
	 * @param result Result (not null if execution succeeded) 
	 * @param error Error ( not null if execution failed)
	 */
	<WR> void putResult(final String workId, WR result, Throwable error);

	void putStart(final String workId);
}
