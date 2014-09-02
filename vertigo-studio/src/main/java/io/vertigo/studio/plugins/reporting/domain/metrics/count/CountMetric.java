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
package io.vertigo.studio.plugins.reporting.domain.metrics.count;

import io.vertigo.core.lang.Assertion;
import io.vertigo.studio.reporting.Metric;

/**
 * @author pchretien
 * Résultat du plugin JoinCount
 */
public final class CountMetric implements Metric {
	private final Integer count;
	private final Status status;

	/**
	 * Constructeur par défaut.
	 * @param count Nombre de lignes
	 * @param status Etat du test
	 */
	public CountMetric(final Integer count, final Status status) {
		Assertion.checkNotNull(status);
		switch (status) {
			case Rejected:
			case Error:
				Assertion.checkState(count == null, "count must be null ");
				break;
			case Executed:
				Assertion.checkNotNull(count);
				break;
			default:
				throw new IllegalArgumentException("case " + status + " not implemented");
		}
		//---------------------------------------------------------------------
		this.status = status;
		this.count = count;
	}

	/** {@inheritDoc} */
	public String getTitle() {
		return "Nbre lignes";
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
		return "rows";
	}

	/** {@inheritDoc} */
	public Status getStatus() {
		return status;
	}

}
