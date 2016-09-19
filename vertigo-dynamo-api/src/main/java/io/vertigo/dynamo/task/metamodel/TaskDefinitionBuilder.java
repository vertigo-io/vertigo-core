/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

/**
 * Builder of taskDefinition.
 *
 * @author  fconstantin, pchretien
 */
public final class TaskDefinitionBuilder implements Builder<TaskDefinition> {
	private final List<TaskAttribute> myInTaskAttributes = new ArrayList<>();
	private TaskAttribute myOutTaskAttribute;
	private final String myTaskDefinitionName;
	private Class<? extends TaskEngine> myTaskEngineClass;
	private String myRequest;
	private String myPackageName;
	private String myDataSpace;

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
	 * @return this builder
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
	 * @return this builder
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
	 * @return this builder
	 */
	public TaskDefinitionBuilder withPackageName(final String packageName) {
		//packageName peut être null
		//-----
		myPackageName = packageName;
		return this;
	}

	/**
	 * Sets the dataSpace
	 * @param dataSpace the dataSpace
	 * @return this builder
	 */
	public TaskDefinitionBuilder withDataSpace(final String dataSpace) {
		//dataSpace can be null
		//-----
		myDataSpace = dataSpace;
		return this;
	}

	/**
	 * Adds an input attribute.
	 *
	 * @param attributeName the name of the attribute
	 * @param domain the domain of the attribute
	 * @param required if attribute is required
	 * @return this builder
	 */
	public TaskDefinitionBuilder addInAttribute(final String attributeName, final Domain domain, final boolean required) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//-----
		final TaskAttribute taskAttribute = new TaskAttribute(attributeName, domain, required);
		myInTaskAttributes.add(taskAttribute);
		return this;
	}

	/**
	 * Adds an output attribute.
	 *
	 * @param attributeName the name of the attribute
	 * @param domain the domain of the attribute
	 * @param required if attribute is required
	 * @return this builder
	 */
	public TaskDefinitionBuilder withOutAttribute(final String attributeName, final Domain domain, final boolean required) {
		//-----
		myOutTaskAttribute = new TaskAttribute(attributeName, domain, required);
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public TaskDefinition build() {
		return new TaskDefinition(
				myTaskDefinitionName,
				myPackageName,
				myDataSpace == null ? StoreManager.MAIN_DATA_SPACE_NAME : myDataSpace,
				myTaskEngineClass,
				myRequest,
				myInTaskAttributes,
				Optional.ofNullable(myOutTaskAttribute));
	}

}
