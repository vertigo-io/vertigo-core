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
package io.vertigo.studio.plugins.reporting.task.metrics.performance;

import io.vertigo.studio.reporting.Metric;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Composant d'afficahge des résultats du plugin de performance.
 *	
 * @author tchassagnette
 */
public final class PerformanceMetric implements Metric {
	private final Long executionTime;
	private final Throwable throwable;
	private final Status status;

	/**
	 * Constructeur.
	 * @param executionTime Temps d'execution
	 */
	public PerformanceMetric(final long executionTime) {
		status = Status.Executed;
		this.executionTime = executionTime;
		throwable = null;
	}

	/**
	 * Constructeur en cas de rejet.
	 */
	public PerformanceMetric() {
		status = Status.Rejected;
		executionTime = null;
		throwable = null;
	}

	/**
	 * Constructeur en cas d'erreur.
	 * @param throwable erreur
	 */
	public PerformanceMetric(final Throwable throwable) {
		status = Status.Error;
		this.throwable = throwable;
		executionTime = null;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Temps d'exécution";
	}

	/** {@inheritDoc} */
	public Long getValue() {
		return executionTime;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		if (status != Status.Error) {
			return null;
		}
		final StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	/** {@inheritDoc} */
	public String getUnit() {
		return "ms";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return status;
	}
}
