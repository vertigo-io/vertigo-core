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
package io.vertigo.dynamo.plugins.environment.registries.task;

import io.vertigo.app.Home;
import io.vertigo.core.definition.dsl.dynamic.DslDefinition;
import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

/**
 * @author pchretien
 */
public final class TaskDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {
	/**
	 * Constructeur.
	 */
	public TaskDynamicRegistryPlugin() {
		super(new TaskGrammar());
	}

	/** {@inheritDoc} */
	@Override
	public Definition createDefinition(final DefinitionSpace definitionSpace, final DslDefinition dslDefinition) {
		final DslEntity dslEntity = dslDefinition.getEntity();

		if (TaskGrammar.TASK_DEFINITION_ENTITY.equals(dslEntity)) {
			//Only taskDefinitions are concerned
			return createTaskDefinition(dslDefinition);
		}
		throw new IllegalStateException("The type of definition" + dslDefinition + " is not managed by me");
	}

	private static Class<? extends TaskEngine> getTaskEngineClass(final DslDefinition xtaskDefinition) {
		final String taskEngineClassName = getPropertyValueAsString(xtaskDefinition, KspProperty.CLASS_NAME);
		return ClassUtil.classForName(taskEngineClassName, TaskEngine.class);
	}

	private static TaskDefinition createTaskDefinition(final DslDefinition xtaskDefinition) {
		final String taskDefinitionName = xtaskDefinition.getName();
		final String request = getPropertyValueAsString(xtaskDefinition, KspProperty.REQUEST);
		Assertion.checkNotNull(taskDefinitionName);
		final Class<? extends TaskEngine> taskEngineClass = getTaskEngineClass(xtaskDefinition);
		final String dataSpace = (String) xtaskDefinition.getPropertyValue(KspProperty.DATA_SPACE);
		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskDefinitionName)
				.withEngine(taskEngineClass)
				.withDataSpace(dataSpace)
				.withRequest(request)
				.withPackageName(xtaskDefinition.getPackageName());
		for (final DslDefinition xtaskAttribute : xtaskDefinition.getChildDefinitions(TaskGrammar.TASK_ATTRIBUTE)) {
			final String attributeName = xtaskAttribute.getName();
			Assertion.checkNotNull(attributeName);
			final String domainName = xtaskAttribute.getDefinitionLinkName("domain");
			final Domain domain = Home.getApp().getDefinitionSpace().resolve(domainName, Domain.class);
			//-----
			final Boolean required = getPropertyValueAsBoolean(xtaskAttribute, KspProperty.NOT_NULL);
			if (isInValue(getPropertyValueAsString(xtaskAttribute, KspProperty.IN_OUT))) {
				if (required.booleanValue()) {
					taskDefinitionBuilder.addInRequired(attributeName, domain);
				} else {
					taskDefinitionBuilder.addInOptional(attributeName, domain);
				}
			} else {
				if (required.booleanValue()) {
					taskDefinitionBuilder.withOutRequired(attributeName, domain);
				} else {
					taskDefinitionBuilder.withOutOptional(attributeName, domain);
				}
			}
		}
		return taskDefinitionBuilder.build();
	}

	private static boolean isInValue(final String sText) {
		switch (sText) {
			case "in":
				return true;
			case "out":
				return false;
			default:
				throw new IllegalArgumentException("les seuls types autorises sont 'in' ou 'out' et non > " + sText);
		}
	}
}
