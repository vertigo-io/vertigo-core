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
/**
 *
 */
package io.vertigo.commons.health;

import java.util.List;

import io.vertigo.core.component.Component;

/**
 * This component checks the health of the current application.
 *
 * @author jmforhan
 */
public interface HealthManager extends Component {
	/**
	 * @return the list of health checks
	 */
	List<HealthCheck> getHealthChecks();

	/**
	 * Generates an aggregated status from a list of health checks.
	 *
	 * @param healthChecks the list of halth checks.
	 * @return the global health status
	 */
	HealthStatus aggregate(List<HealthCheck> healthChecks);
}
