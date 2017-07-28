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
package io.vertigo.commons.metric;

/**
 * Interface représentant un plugin d'analyse.
 *
 * @author tchassagnette
 * @param <I> Tyep d'objet dont on souhaite obtenir la métrique
 */
public interface MetricEngine<I> {
	/**
	 * Moteur permettant de calculer une métrique.
	 * @param item item to benchmark
	 * @return Métrique obtenue.
	 */
	Metric execute(final I item);

	default boolean isApplicable(final I item) {
		return true;
	}
}