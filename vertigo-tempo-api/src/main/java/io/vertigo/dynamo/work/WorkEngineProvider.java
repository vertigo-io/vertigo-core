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
package io.vertigo.dynamo.work;

import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * Provider des taches.
 * Ce provider définit le moyen dont la tache doit être exécuter.
 * Dans la plupart des cas le moyen est une classe.
 * Dans certain cs il peut s'agir du nom de la classe.
 * @author  pchretien
 */
public final class WorkEngineProvider<WR, W> {
	private final String className;
	private final Class<? extends WorkEngine<WR, W>> clazz;
	private final WorkEngine<WR, W> workEngine;

	public WorkEngineProvider(final Class<? extends WorkEngine<WR, W>> clazz) {
		Assertion.checkNotNull(clazz);
		//-----------------------------------------------------------------
		this.clazz = clazz;
		this.className = clazz.getName();
		this.workEngine = null;
	}

	public WorkEngineProvider(final WorkEngine<WR, W> workEngine) {
		Assertion.checkNotNull(workEngine);
		//-----------------------------------------------------------------
		this.workEngine = workEngine;
		this.clazz = null;
		this.className = workEngine.getClass().getName();
	}

	public WorkEngineProvider(final String className) {
		Assertion.checkArgNotEmpty(className);
		//-----------------------------------------------------------------
		this.className = className;
		this.clazz = null;
		this.workEngine = null;
	}

	public WorkEngine<WR, W> provide() {
		if (workEngine != null) {
			return workEngine;
		}
		final Class<? extends WorkEngine<WR, W>> engineClazz;
		if (clazz != null) {
			engineClazz = clazz;
		} else {
			engineClazz = (Class<? extends WorkEngine<WR, W>>) ClassUtil.classForName(className);
		}
		//récupéartion de l'engine par sa classe.
		return Injector.newInstance(engineClazz, Home.getComponentSpace());
	}

	public String getName() {
		return className;
	}

}
