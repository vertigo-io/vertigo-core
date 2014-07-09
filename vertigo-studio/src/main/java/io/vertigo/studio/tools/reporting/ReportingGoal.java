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
package io.vertigo.studio.tools.reporting;

import io.vertigo.kernel.Home;
import io.vertigo.studio.reporting.ReportingManager;
import io.vertigo.studio.tools.Goal;

import java.util.Properties;

/**
 * @author pchretien
 */
public final class ReportingGoal implements Goal {

	public void process(final Properties properties) {
		final ReportingManager reportingManager = Home.getComponentSpace().resolve(ReportingManager.class);

		reportingManager.analyze();
	}
}
