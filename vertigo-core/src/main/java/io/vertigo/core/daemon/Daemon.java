/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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

/**
 * This interface serves as a marker to identify daemons, distinguishing them from simple other runnables.
 * Daemons are typically used for background tasks and are expected to run continuously.
 *
 * Implementing this interface indicates that a class represents a daemon.
 *
 * @author: mlaroche, pchretien, npiedeloup
 *
 * @see Runnable
 */
public interface Daemon extends Runnable {
	/**
     * Determines whether verbose logging is activated for this daemon.
     *
     * Verbose logging provides detailed information about the daemon's activities and can be
     * useful for debugging or monitoring purposes.
     *
     * @return {@code true} if verbose logging is activated, {@code false} otherwise.
     */
	default boolean verbose() {
		return false;
	}
}
