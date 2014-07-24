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
package io.vertigo.dynamo.task.metamodel;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/** Builder des définitions de taches.
 *
 * @author  fconstantin, pchretien
 */
public final class TaskDefinitionBuilder implements Builder<TaskDefinition> {
	private final List<TaskAttribute> taskAttributes = new ArrayList<>();

	private final String taskDefinitionName;
	private Class<? extends TaskEngine> taskEngineClass;
	private String request;
	private String packageName;

	/**
	 * Construction du builder.
	 *
	 * @param taskDefinitionName Nom de la définition de la tache
	 */
	public TaskDefinitionBuilder(final String taskDefinitionName) {
		Assertion.checkNotNull(taskDefinitionName);
		//----------------------------------------------------------------------
		this.taskDefinitionName = taskDefinitionName;
	}

	/**
	 * Initialise une définition de tache.
	 *
	 * @param taskEngineClass Classe réalisant l'implémentation
	 */
	public TaskDefinitionBuilder withEngine(final Class<? extends TaskEngine> taskEngineClass) {
		Assertion.checkNotNull(taskEngineClass);
		//Il est important de refaire le test car les test de cast ne sont pas fiable avec les generics
		if (taskEngineClass.isAssignableFrom(TaskEngine.class)) {
			throw new ClassCastException("La classe doit être une sous classe de ServiceProvider");
		}
		//---------------------------------------------------------------------
		this.taskEngineClass = taskEngineClass;
		return this;
	}

	/**
	 * @param request Chaine de configuration de la tache
	 */
	public TaskDefinitionBuilder withRequest(final String request) {
		Assertion.checkNotNull(request);
		//---------------------------------------------------------------------
		//Pour unifier la saisie de la request sous un environnement unix ou dos
		// et pour éviter la disparité de gestion des retours chariot
		//par certains drivers de base de données.
		this.request = request.replace("\r", "");
		return this;
	}

	/**
	 * @param packageName Nom du package
	 */
	public TaskDefinitionBuilder withPackageName(final String packageName) {
		//packageName peut être null
		//---------------------------------------------------------------------
		this.packageName = packageName;
		return this;
	}

	/**
	 * Ajoute un attribut à une définition de tache.
	 *
	 * @param attributeName Nom de l'attribut
	 * @param domain Domaine de l'attribut
	 * @param notNull Si attribut obligatoirement non null
	 * @param in Si attribut entrant
	 */
	public TaskDefinitionBuilder withAttribute(final String attributeName, final Domain domain, final boolean notNull, final boolean in) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//----------------------------------------------------------------------
		final TaskAttribute taskAttribute = new TaskAttribute(attributeName, domain, notNull, in);
		taskAttributes.add(taskAttribute);
		return this;
	}

	/** {@inheritDoc} */
	public TaskDefinition build() {
		return new TaskDefinition(taskDefinitionName, packageName, taskEngineClass, request, taskAttributes);
	}
}
