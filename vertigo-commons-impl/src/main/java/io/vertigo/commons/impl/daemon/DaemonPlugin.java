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
package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.lang.Plugin;

/**
 * Plugin de gestion des démons.
 *
 * @author TINGARGIOLA
 */
public interface DaemonPlugin extends Plugin {

	/**
	 * Enregistre un démon. Il sera lancé après le temp delay (en milliseconde) et sera réexécuté périodiquement toute
	 * les period (en milliseconde).
	 *
	 * @param daemonName Nom du démon (DMN_XXX)
	 * @param daemon Le démon à lancer.
	 * @param periodInSeconds La période d'exécution du démon.
	 */
	void scheduleDaemon(String daemonName, Daemon daemon, long periodInSeconds);
}
