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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures periodic execution of daemon methods.
 * Allows scheduling methods to run as background tasks
 * with specified intervals and monitoring options.
 *
 * @author mlaroche
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DaemonScheduled {

	/**
	 * Unique identifier for the scheduled daemon.
	 * @return Daemon name
	 */
	String name();

	/**
	 * Time between daemon executions.
	 * @return Interval in seconds
	 */
	int periodInSeconds();

	/**
	 * Enables analytics monitoring for the daemon.
	 * @return Whether to collect execution metrics
	 */
	boolean analytics() default true;
}
