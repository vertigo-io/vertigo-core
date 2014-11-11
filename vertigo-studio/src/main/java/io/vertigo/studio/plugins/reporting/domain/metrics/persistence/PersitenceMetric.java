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
package io.vertigo.studio.plugins.reporting.domain.metrics.persistence;

import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * Résultat du plugin JoinCount
 */
public final class PersitenceMetric implements Metric {
	private final boolean persistent;
	private final Status status;

	/**
	 * Constructeur par défaut.
	 * @param persistent Si persistent
	 * @param test Si test Ok
	 */
	public PersitenceMetric(final boolean persistent, final boolean test) {
		this.persistent = persistent;
		if (test) {
			status = Status.Executed;
		} else {
			status = Status.Error;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getTitle() {
		return "Persistance";
	}

	/** {@inheritDoc} */
	@Override
	public Boolean getValue() {
		return persistent;
	}

	/** {@inheritDoc} */
	@Override
	public String getValueInformation() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String getUnit() {
		return "";
	}

	/** {@inheritDoc} */
	@Override
	public Status getStatus() {
		return status;
	}
}
