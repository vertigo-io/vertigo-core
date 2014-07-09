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
package io.vertigo.studio.reporting;

/**
 * Interface décrivant un résultat de metric.
 * 
 * @author tchassagnette, pchretien
 */
public interface Metric {
	enum Status {
		/** Exécution OK*/
		Executed,
		/** Erreur lors de l'exécution*/
		Error,
		/** Métrique non pertinente*/
		Rejected
	}

	/**
	 * @return Status de la métrique.
	 */
	Status getStatus();

	/**
	 * @return Titre de la métrique. (notNull)
	 */
	String getTitle();

	/**
	 * @return Unité de la métrique. (notNull)
	 */
	String getUnit();

	/**
	 * @return Valeur de la métrique. (Integer, Long, String, etc..) 
	 */
	Object getValue();

	/**
	 * @return Complément d'information sur la valeur. (nullable)
	 */
	String getValueInformation();
}
