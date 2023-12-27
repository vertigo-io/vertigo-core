/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon;

import java.util.List;

import io.vertigo.core.node.component.Manager;

/**
 * Manages daemons.
 * A daemon is a technical component as opposed to a job or batch.
 * DaemonManager extends the functionality of the Manager interface.
 *
 * This interface defines methods for managing and interacting with daemons.
 * Daemons are technical components that are expected to run continuously in the background.
 *
 * Authors: mlaroche, pchretien, npiedeloup
 *
 * @see Manager
 */
public interface DaemonManager extends Manager {
	/**
     * Provides a snapshot or copy of the execution statistics for all managed daemons.
     *
     * The returned list of DaemonStat objects contains information about the current
     * execution statistics of each daemon managed by this DaemonManager.
     *
     * @return a list of DaemonStat objects representing the execution statistics of managed daemons.
     */
	List<DaemonStat> getStats();
}
