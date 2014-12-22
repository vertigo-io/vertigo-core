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
package io.vertigo.tempo.job.metamodel;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * JobDefinition décrit le travail qu'il faut exécuter.
 * La tache à exécuter oit étendre Runnable.
 * @author pchretien
 */
@DefinitionPrefix("JB")
public final class JobDefinition implements Definition {
	/** Nom de la définition. */
	private final String name;
	private final Class<? extends Runnable> jobClass;

	/**
	 * Constructeur
	 * @param name Nom de la définition
	 */
	public JobDefinition(final String name, final Class<? extends Runnable> jobClass) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(jobClass);
		//-----
		this.name = name;
		this.jobClass = jobClass;
	}

	/**
	 * Retourne la classe réalisant l'implémentation de la tache.
	 * @return Classe réalisant l'implémentation
	 */
	public Class<? extends Runnable> getJobClass() {
		return jobClass;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
