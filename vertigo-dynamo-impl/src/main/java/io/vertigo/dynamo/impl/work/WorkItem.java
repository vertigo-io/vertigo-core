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
package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.lang.Assertion;

public final class WorkItem<WR, W> {
	private final W work;
	private final WorkEngineProvider<WR, W> workEngineProvider;
	private final String id;

	/**
	 * Constructor. 
	 * This workItem is used to define a synchronous work. 
	 * @param work Travail dont on représente l'état.
	 */
	public WorkItem(final String id, final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		Assertion.checkNotNull(id);
		//work can be null
		Assertion.checkNotNull(workEngineProvider);
		//---------------------------------------------------------------------
		this.id = id;
		this.work = work;
		this.workEngineProvider = workEngineProvider;
	}

	public String getId() {
		return id;
	}

	/**
	 * Permet de récupérer les informations pour réaliser un traitement. 
	 * @return le work
	 */
	public W getWork() {
		return work;
	}

	public WorkEngineProvider<WR, W> getWorkEngineProvider() {
		return workEngineProvider;
	}
}
