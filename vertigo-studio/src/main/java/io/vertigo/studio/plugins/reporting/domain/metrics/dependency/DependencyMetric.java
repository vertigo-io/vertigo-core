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
package io.vertigo.studio.plugins.reporting.domain.metrics.dependency;

import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * Résultat du plugin JoinCount
 */
public final class DependencyMetric implements Metric {
	private final int count;

	/**
	 * Constructeur par défaut.
	 * @param count Nombre de reférence
	 */
	public DependencyMetric(final int count) {
		this.count = count;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Utilisation dans les dao";
	}

	/** {@inheritDoc} */
	public Integer getValue() {
		return count;
	}

	/** {@inheritDoc} */
	public String getValueInformation() {
		return null;
	}

	/** {@inheritDoc} */
	public String getUnit() {
		return "";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return Status.Executed;
	}

}
