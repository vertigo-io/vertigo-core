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
package io.vertigo.commons.impl.metric;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.commons.metric.Metric;
import io.vertigo.commons.metric.MetricManager;
import io.vertigo.lang.Assertion;

/**
 * Implémentation de ReportingManager.
 *
 * @author pchretien
 */
public final class MetricManagerImpl implements MetricManager {
	private final List<MetricPlugin> reportingPlugins;

	/**
	 * @param reportingPlugins reportingPlugins
	 */
	@Inject
	public MetricManagerImpl(
			final List<MetricPlugin> reportingPlugins) {
		Assertion.checkNotNull(reportingPlugins);
		//-----
		this.reportingPlugins = reportingPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public List<Metric> analyze() {
		return reportingPlugins
				.stream()
				.flatMap(plugin -> plugin.analyze().stream())
				.collect(Collectors.toList());
	}

}
