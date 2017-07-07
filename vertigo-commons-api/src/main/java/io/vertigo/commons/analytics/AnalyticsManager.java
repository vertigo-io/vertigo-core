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
package io.vertigo.commons.analytics;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import io.vertigo.core.component.Manager;

/**
 * Main access to all analytics functions.
 *
 * @author pchretien, npiedeloup
 */
public interface AnalyticsManager extends Manager {

	/**
	 * Traces a process and collects metrics during its execution.
	 * A traced process is stored by categories.
	 * @param category  the category of the process
	 * @param name the name of the process
	 * @param consumer the function to execute within the tracer
	 */
	void trace(final String category, final String name, Consumer<AnalyticsTracer> consumer);

	/**
	 * Traces a process that has a return value (and collects metrics during its execution).
	 * A traced process is stored by categories.
	 * @param category the category of the process
	 * @param name the name of the process
	 * @param function the function to execute within the tracer
	 * @return the result of the traced function
	 */
	<O> O traceWithReturn(final String category, final String name, Function<AnalyticsTracer, O> function);

	/**
	 * @return the current tracer if it has been created before
	 */
	Optional<AnalyticsTracer> getCurrentTracer();

}
