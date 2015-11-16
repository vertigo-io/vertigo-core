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
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;
import io.vertigo.lang.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder of taskDefinition.
 *
 * @author  fconstantin, pchretien
 */
public final class TaskDefinitionBuilder implements Builder<TaskDefinition> {
	private static final String DEFAULT_STORE_NAME = "main";

	private final List<TaskAttribute> myInTaskAttributes = new ArrayList<>();
	private TaskAttribute myOutTaskAttribute;
	private final String myTaskDefinitionName;
	private Class<? extends TaskEngine> myTaskEngineClass;
	private String myRequest;
	private String myPackageName;
	private String myStoreName;

	/**
	 * Constructor.
	 *
	 * @param taskDefinitionName Name (TK_XXX_YYY)
	 */
	public TaskDefinitionBuilder(final String taskDefinitionName) {
		Assertion.checkNotNull(taskDefinitionName);
		//-----
		myTaskDefinitionName = taskDefinitionName;
	}

	/**
	 * Defines the engine, used at runtime to process the task.
	 *
	 * @param taskEngineClass Class running the task
	 */
	public TaskDefinitionBuilder withEngine(final Class<? extends TaskEngine> taskEngineClass) {
		Assertion.checkNotNull(taskEngineClass);
		//Il est important de refaire le test car les test de cast ne sont pas fiable avec les generics
		if (taskEngineClass.isAssignableFrom(TaskEngine.class)) {
			throw new ClassCastException("La classe doit être une sous classe de ServiceProvider");
		}
		//-----
		myTaskEngineClass = taskEngineClass;
		return this;
	}

	/**
	 * @param request Request used to configure the task. (ldap request, sql request...)
	 */
	public TaskDefinitionBuilder withRequest(final String request) {
		Assertion.checkNotNull(request);
		//-----
		//Pour unifier la saisie de la request sous un environnement unix ou dos
		// et pour éviter la disparité de gestion des retours chariot
		//par certains drivers de base de données.
		myRequest = request.replace("\r", "");
		return this;
	}

	/**
	 * @param packageName Name of the package
	 */
	public TaskDefinitionBuilder withPackageName(final String packageName) {
		//packageName peut être null
		//-----
		myPackageName = packageName;
		return this;
	}

	public TaskDefinitionBuilder withStore(final String storeName) {
		//storeName may be null
		//-----
		myStoreName = storeName;
		return this;
	}

	/**
	 * Add an input attribute.
	 *
	 * @param attributeName Name of the attribute
	 * @param domain Domain of the attribute
	 * @param notNull If attribute must be not null
	 */
	public TaskDefinitionBuilder addInAttribute(final String attributeName, final Domain domain, final boolean notNull) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//-----
		final TaskAttribute taskAttribute = new TaskAttribute(attributeName, domain, notNull, true);
		myInTaskAttributes.add(taskAttribute);
		return this;
	}

	/**
	 * Add an output attribute.
	 *
	 * @param attributeName Name of the attribute
	 * @param domain Domain of the attribute
	 * @param notNull If attribute must be not null
	 */
	public TaskDefinitionBuilder withOutAttribute(final String attributeName, final Domain domain, final boolean notNull) {
		//-----
		myOutTaskAttribute = new TaskAttribute(attributeName, domain, notNull, false);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public TaskDefinition build() {
		return new TaskDefinition(
				myTaskDefinitionName,
				myPackageName,
				myStoreName == null ? DEFAULT_STORE_NAME : myStoreName,
				myTaskEngineClass,
				myRequest,
				myInTaskAttributes,
				Option.option(myOutTaskAttribute));
	}

}
