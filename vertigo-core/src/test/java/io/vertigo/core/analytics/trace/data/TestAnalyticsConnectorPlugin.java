/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.analytics.trace.data;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;

public class TestAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {
	private static int count = 0;
	private static String lastCategory;
	private static Double lastPrice;

	@Override
	public void add(final TraceSpan span) {
		count++;
		lastCategory = span.getCategory();
		lastPrice = span.getMeasures().get("price");
	}

	@Override
	public void add(final Metric metric) {
		// nothing

	}

	@Override
	public void add(final HealthCheck healthCheck) {
		// nothing

	}

	public static int getCount() {
		return count;
	}

	public static String getLastcategory() {
		return lastCategory;
	}

	public static void reset() {
		count = 0;
		lastCategory = null;
		lastPrice = null;
	}

	public static Double getLastPrice() {
		return lastPrice;
	}

}
